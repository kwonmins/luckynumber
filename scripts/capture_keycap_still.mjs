import { createRequire } from "node:module";
import { mkdir } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, "..");
const htmlPath = resolve(root, "generated", "korean_keycap_asmr.html");
const outputPath = resolve(root, "generated", "korean_keycap_asmr_still.png");
const require = createRequire(import.meta.url);
const { chromium } = require("C:/Users/USER/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright");

await mkdir(dirname(outputPath), { recursive: true });

const browser = await chromium.launch({
  headless: true,
  executablePath: "C:/Program Files/Google/Chrome/Application/chrome.exe",
  args: ["--use-fake-ui-for-media-stream"],
});

try {
  const page = await browser.newPage({
    viewport: { width: 720, height: 1280 },
    deviceScaleFactor: 1,
  });
  await page.goto(pathToFileURL(htmlPath).href);
  await page.evaluate(() => window.draw(3.2));
  await page.screenshot({ path: outputPath, fullPage: false });
  console.log(outputPath);
} finally {
  await browser.close();
}
