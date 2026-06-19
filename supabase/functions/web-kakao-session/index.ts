import { createClient } from "jsr:@supabase/supabase-js@2";

type KakaoAccount = {
  email?: string;
  profile?: {
    nickname?: string;
    thumbnail_image_url?: string;
    profile_image_url?: string;
  };
};

type KakaoUser = {
  id?: number;
  kakao_account?: KakaoAccount;
};

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json({ error: "method_not_allowed" }, 405);

  const { accessToken } = await req.json().catch(() => ({ accessToken: "" }));
  if (typeof accessToken !== "string" || accessToken.trim().length === 0) {
    return json({ error: "missing_kakao_access_token" }, 400);
  }

  const kakaoUser = await fetchKakaoUser(accessToken).catch((error) => {
    console.error(error);
    return null;
  });
  if (!kakaoUser?.id) return json({ error: "invalid_kakao_access_token" }, 401);

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = getServiceRoleKey();
  if (!supabaseUrl || !serviceRoleKey) return json({ error: "server_not_configured" }, 500);

  const providerUserId = String(kakaoUser.id);
  const account = kakaoUser.kakao_account;
  const profile = account?.profile;
  const userId = `kakao:${providerUserId}`;
  const displayName = profile?.nickname?.trim() || "카카오 사용자";
  const avatarUrl = profile?.thumbnail_image_url || profile?.profile_image_url || null;
  const sessionToken = createToken();
  const tokenHash = await sha256Hex(sessionToken);
  const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();

  const admin = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false },
    global: {
      headers: {
        apikey: serviceRoleKey,
        Authorization: `Bearer ${serviceRoleKey}`,
      },
    },
  });

  const { error: userError } = await admin.from("web_users").upsert(
    {
      id: userId,
      provider: "kakao",
      provider_user_id: providerUserId,
      display_name: displayName,
      email: account?.email ?? null,
      avatar_url: avatarUrl,
      last_login_at: new Date().toISOString(),
    },
    { onConflict: "id" },
  );
  if (userError) return json({ error: "user_upsert_failed", detail: userError.message }, 500);

  const { error: sessionError } = await admin.from("web_sessions").upsert(
    {
      token_hash: tokenHash,
      user_id: userId,
      provider: "kakao",
      provider_user_id: providerUserId,
      expires_at: expiresAt,
      last_used_at: new Date().toISOString(),
    },
    { onConflict: "token_hash" },
  );
  if (sessionError) return json({ error: "session_upsert_failed", detail: sessionError.message }, 500);

  return json({
    user: {
      id: userId,
      provider: "kakao",
      providerUserId,
      displayName,
      email: account?.email ?? null,
      avatarUrl,
    },
    sessionToken,
    expiresAt,
  });
});

async function fetchKakaoUser(accessToken: string): Promise<KakaoUser> {
  const response = await fetch("https://kapi.kakao.com/v2/user/me", {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!response.ok) throw new Error(`kakao_user_fetch_failed:${response.status}`);
  return await response.json();
}

function getServiceRoleKey(): string | null {
  const direct = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (direct) return direct;

  const secretKeys = Deno.env.get("SUPABASE_SECRET_KEYS");
  if (!secretKeys) return null;

  try {
    const parsed = JSON.parse(secretKeys);
    return parsed.service_role ?? parsed.serviceRole ?? null;
  } catch {
    return null;
  }
}

function createToken(): string {
  const bytes = new Uint8Array(32);
  crypto.getRandomValues(bytes);
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
}

async function sha256Hex(value: string): Promise<string> {
  const bytes = new TextEncoder().encode(value);
  const digest = await crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(digest)).map((byte) => byte.toString(16).padStart(2, "0")).join("");
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
