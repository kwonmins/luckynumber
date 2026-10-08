import { createRequire } from 'node:module';
import { readFile, mkdir } from 'node:fs/promises';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import vm from 'node:vm';
const require = createRequire(import.meta.url);
const { chromium } = require('C:/Users/USER/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const fragmentPath = 'C:/Users/USER/.codex/visualizations/2026/10/07/01a118c9-cc95-7a21-819e-0e814a885a0d/suri-premium-cover-concepts.html';
const html = await readFile(fragmentPath, 'utf8');
for (const block of html.matchAll(/<script>([\s\S]*?)<\/script>/g)) new vm.Script(block[1]);
if (html.includes('\\n') || html.includes('\\"')) throw new Error('Escaped literal markup');
const out = resolve('output/premium-cover-concepts-2026-10-08');
await mkdir(out, {recursive:true});
const browser = await chromium.launch({headless:true, executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe'});
const errors=[];
try {
  const page = await browser.newPage({viewport:{width:768,height:1120},deviceScaleFactor:1});
  page.on('pageerror',e=>errors.push(e.message));
  await page.goto(pathToFileURL(resolve('design_previews/premium-cover-concepts-preview.html')).href);
  const inner=page.frameLocator('iframe');
  const moon=inner.locator('.suri-app[data-design="moon"]');
  await moon.locator('.suri-topic-button').first().waitFor();
  await moon.locator('.suri-topic-button svg').first().waitFor({timeout:20000});
  if(await moon.locator('.suri-topic-button').count()!==9)throw new Error('Missing personal topics');
  await moon.screenshot({path:resolve(out,'moon-library.png')});
  const topics=['romance','career','money','study','health','business','general','self','relationship'];
  for(const id of topics){
    await moon.locator(`[data-topic-select="${id}"]`).click();
    if(await moon.locator('.suri-fan-book[data-offset="0"] .suri-cover').getAttribute('data-topic')!==id)throw new Error('Selection failed: '+id);
    if(await moon.locator('.suri-art-main i[data-lucide]').count())throw new Error('Missing lucide replacement');
    const readable=await moon.locator('.suri-fan-book[data-offset="0"] .suri-cover').evaluate(el=>{const kind=el.querySelector('.suri-cover-kind').getBoundingClientRect(),foot=el.querySelector('.suri-cover-bottom').getBoundingClientRect();return foot.top>kind.bottom+8;});
    if(!readable)throw new Error('Cover title overlaps footer: '+id);
  }
  await moon.locator('[data-topic-select="self"]').click();
  await moon.locator('.suri-open').click();
  await moon.screenshot({path:resolve(out,'moon-self-cover.png')});
  await moon.locator('.suri-close').click();
  await moon.locator('[data-mode="compatibility"]').click();
  if(await moon.locator('.suri-topic-button').count()!==3)throw new Error('Missing compatibility topics');
  for(const id of ['couple','crush','reunion'])await moon.locator(`[data-topic-select="${id}"]`).click();
  await moon.locator('[data-mode="personal"]').click();
  await moon.locator('[data-topic-select="romance"]').click();
  const frame=page.frames().find(f=>f!==page.mainFrame());
  await frame.evaluate(()=>{const variants=document.querySelectorAll('#suri-cover-concepts [data-variant]');variants.forEach((el,i)=>el.hidden=i===0);});
  const spine=inner.locator('.suri-app[data-design="spine"]');
  await spine.locator('[data-topic-select="career"]').click();
  await spine.screenshot({path:resolve(out,'spine-library.png')});
  await spine.locator('.suri-open').click();
  await spine.screenshot({path:resolve(out,'spine-career-cover.png')});
  await spine.locator('.suri-close').click();
  for(const width of [320,390,736]){
    await page.setViewportSize({width,height:1120});
    const dimensions=await frame.evaluate(()=>({width:document.documentElement.clientWidth,scroll:document.documentElement.scrollWidth}));
    if(dimensions.scroll>dimensions.width+1)throw new Error('Horizontal overflow at '+width+': '+JSON.stringify(dimensions));
    await spine.locator('[data-topic-select="relationship"]').click();
    await spine.locator('.suri-open').click();
    const fits=await spine.locator('.suri-large-cover').evaluate(el=>{const p=el.parentElement.getBoundingClientRect(),r=el.getBoundingClientRect();return r.left>=p.left&&r.right<=p.right;});
    if(!fits)throw new Error('Large cover overflow at '+width);
    await spine.locator('.suri-close').click();
  }
  await page.setViewportSize({width:768,height:1120});
  await page.emulateMedia({colorScheme:'dark'});
  await spine.screenshot({path:resolve(out,'spine-dark.png')});
  if(errors.length)throw new Error(errors.join('\n'));
  console.log(JSON.stringify({ok:true,personalTopics:9,compatibilityTopics:3,variants:2,responsiveWidths:[320,390,736],pageErrors:errors},null,2));
} finally {await browser.close();}
