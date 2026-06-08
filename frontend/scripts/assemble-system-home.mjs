import { readFileSync, writeFileSync } from "fs";
import { join, dirname } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const out = join(__dirname, "../src/views/SystemHome.vue");

let raw = readFileSync(join(__dirname, "../_extract_template.txt"), "utf8").replace(/^<user_query>\n?/, "");
const tplEnd = raw.indexOf("</template>") + 11;
let template = raw.slice(0, tplEnd);
let style = raw.slice(raw.indexOf("<style scoped>")).replace(/<\/style>[\s\S]*$/, "</style>");

const patches = [
  ['@click="showAssistant = !showAssistant"', '@click="toggleAssistant"'],
  [
    '<span class="assistant-badge">DeepSeek</span>',
    '<span class="assistant-badge">{{ deepSeekReady ? "DeepSeek" : "演示模式" }}</span>'
  ],
  [
    "    </header>\n\n    <!-- 智能助手弹窗 -->",
    `    </header>

    <div v-if="homeNotice" class="home-notice-bar">
      <div class="container">
        <button type="button" class="home-notice-inner" @click="toastDemo(homeNotice)">
          <span class="home-notice-tag">公告</span>
          <span class="home-notice-text">{{ homeNotice }}</span>
        </button>
      </div>
    </div>

    <!-- 智能助手弹窗 -->`
  ],
  [
    '<RouterLink class="hero-btn outline" to="/register">新用户注册</RouterLink>\n            </motion.div>',
    '<RouterLink class="hero-btn outline" to="/register">新用户注册</RouterLink>\n              <RouterLink v-if="continueTo" class="hero-btn subtle outline" :to="continueTo">继续上次身份</RouterLink>\n            </div>'
  ],
  [
    '<div class="section-title" data-animate="fadeInUp"><h2>数字银发智盾</h2><p>立足养老行业，打造标准、易用、便捷的智慧康养综合管理平台</p></div>',
    '<motion.div class="section-title" data-animate="fadeInUp"><h2>数字银发智盾</h2><p>{{ statsLabel }}</p></div>'
  ],
  [
    '<button class="activity-join">立即报名</button>',
    '<button type="button" class="activity-join" @click="toastDemo(`活动报名为演示占位：${a.title}`)">立即报名</button>'
  ],
  [
    '<button class="life-btn">立即预约</button>',
    '<button type="button" class="life-btn" @click="toastDemo(`生活服务预约为演示占位：${s.title}`)">立即预约</button>'
  ],
  [
    '<p class="footer-tel">演示系统 · 智盾核心 v2.4</p>',
    '<p class="footer-tel">演示系统 · 已建档长者 {{ homeSummary?.elderCount ?? "—" }} 人 · 接入设备 {{ homeSummary?.deviceCount ?? "—" }} 台 · 智盾核心 v2.4</p>'
  ]
];

for (const [from, to] of patches) {
  if (!template.includes(from.split("\n")[0].slice(0, 20))) {
    // hero continueTo fallback
    if (from.includes("hero-btn outline")) {
      template = template.replace(
        '<RouterLink class="hero-btn outline" to="/register">新用户注册</RouterLink>\n            </div>',
        '<RouterLink class="hero-btn outline" to="/register">新用户注册</RouterLink>\n              <RouterLink v-if="continueTo" class="hero-btn subtle outline" :to="continueTo">继续上次身份</RouterLink>\n            </div>'
      );
      continue;
    }
    if (from.includes("数字银发智盾")) {
      template = template.replace(
        '<div class="section-title" data-animate="fadeInUp"><h2>数字银发智盾</h2><p>立足养老行业，打造标准、易用、便捷的智慧康养综合管理平台</p></div>',
        '<div class="section-title" data-animate="fadeInUp"><h2>数字银发智盾</h2><p>{{ statsLabel }}</p></div>'
      );
      continue;
    }
  }
  template = template.replace(from, to);
}

// fix mistaken motion in statsLabel patch
template = template.replace(
  '<motion.div class="section-title" data-animate="fadeInUp"><h2>数字银发智盾</h2><p>{{ statsLabel }}</p></div>',
  '<div class="section-title" data-animate="fadeInUp"><h2>数字银发智盾</h2><p>{{ statsLabel }}</p></div>'
);

const script = readFileSync(join(__dirname, "system-home-script.ts.part"), "utf8");

const extraCss = `
.home-notice-bar { padding: 8px 0; background: #fffbeb; border-bottom: 1px solid #fde68a; margin-top: 70px; }
.home-notice-inner { display:flex;align-items:center;gap:8px;width:100%;border:none;background:transparent;cursor:pointer;font-family:inherit;text-align:left; }
.home-notice-tag { font-size:11px;padding:2px 8px;border-radius:4px;background:#f59e0b;color:#fff; }
.home-notice-text { font-size:12px;color:#92400e;flex:1; }
.hero-btn.subtle { opacity: 0.9; }
`;

style = style.replace("</style>", `${extraCss}\n</style>`);
const vue = `${template}\n\n${script}\n\n${style}\n`;
writeFileSync(out, vue, "utf8");
console.log("lines:", vue.split("\n").length);
