const crypto = require('node:crypto');

const supabaseFunctionUrl = () => process.env.SUPABASE_KAKAO_SESSION_URL || `${process.env.SUPABASE_URL}/functions/v1/kakao-session`;

module.exports = async function handler(req, res) {
  if (req.method !== 'GET') {
    res.setHeader('Allow', 'GET');
    return res.status(405).json({ error: 'method_not_allowed' });
  }

  const clientId = process.env.KAKAO_REST_API_KEY;
  const redirectUri = process.env.KAKAO_REDIRECT_URI;
  if (!clientId || !redirectUri || !process.env.SUPABASE_KAKAO_SESSION_URL && !process.env.SUPABASE_URL) {
    return res.status(503).json({
      error: 'auth_not_configured',
      message: '카카오 로그인 환경변수가 아직 설정되지 않았습니다.',
    });
  }

  const stateCookie = readCookie(req.headers.cookie, 'suri_oauth_state');
  const state = stateCookie || crypto.randomBytes(24).toString('hex');
  if (!req.query.code) {
    setCookie(res, 'suri_oauth_state', state, 600);
    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      response_type: 'code',
      state,
      scope: 'profile_nickname,profile_image,account_email',
    });
    return res.redirect(`https://kauth.kakao.com/oauth/authorize?${params}`);
  }

  if (!req.query.state || req.query.state !== state) {
    return res.status(400).json({ error: 'invalid_oauth_state' });
  }

  try {
    const tokenResponse = await fetch('https://kauth.kakao.com/oauth/token', {
      method: 'POST',
      headers: { 'content-type': 'application/x-www-form-urlencoded;charset=utf-8' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: clientId,
        redirect_uri: redirectUri,
        code: req.query.code,
        ...(process.env.KAKAO_CLIENT_SECRET ? { client_secret: process.env.KAKAO_CLIENT_SECRET } : {}),
      }),
    });
    const token = await tokenResponse.json();
    if (!tokenResponse.ok || !token.access_token) throw new Error('kakao_token_exchange_failed');

    const sessionResponse = await fetch(supabaseFunctionUrl(), {
      method: 'POST',
      headers: {
        'content-type': 'application/json',
        ...(process.env.SUPABASE_ANON_KEY ? { apikey: process.env.SUPABASE_ANON_KEY } : {}),
      },
      body: JSON.stringify({ accessToken: token.access_token }),
    });
    const session = await sessionResponse.json();
    if (!sessionResponse.ok || !session.sessionToken) throw new Error(session.error || 'supabase_session_exchange_failed');

    setCookie(res, 'suri_session', session.sessionToken, 60 * 60 * 24 * 30);
    clearCookie(res, 'suri_oauth_state');
    return res.redirect('/premium?auth=success');
  } catch (error) {
    console.error('[kakao-auth]', error);
    clearCookie(res, 'suri_oauth_state');
    return res.status(502).json({ error: 'kakao_auth_failed', message: '카카오 로그인 연결에 실패했습니다.' });
  }
};

function readCookie(raw, name) {
  return (raw || '').split(';').map(part => part.trim()).find(part => part.startsWith(`${name}=`))?.slice(name.length + 1) || '';
}

function setCookie(res, name, value, maxAge) {
  res.setHeader('Set-Cookie', `${name}=${value}; Path=/; Max-Age=${maxAge}; HttpOnly; Secure; SameSite=Lax`);
}

function clearCookie(res, name) {
  res.setHeader('Set-Cookie', `${name}=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=Lax`);
}
