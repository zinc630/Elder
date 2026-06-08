import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");
const extract = fs.readFileSync(path.join(root, "elder-template-extract.txt"), "utf8");

const templateStart = extract.indexOf("<template>");
const templateEnd = extract.indexOf("</template>") + "</template>".length;
const styleStart = extract.indexOf("<style scoped>");
const styleEnd = extract.indexOf("</style>") + "</style>".length;

let template = extract.slice(templateStart, templateEnd);
let styles = extract.slice(styleStart, styleEnd);

template = template
  .replace(
    '          <div class="grid-item" @click="onFamily">\n            <div class="grid-icon purple-bg">🎨</motion.div>',
    '          <div class="grid-item" @click="onPortalActivity">\n            <div class="grid-icon purple-bg">🎨</div>'
  )
  .replace(
    '          <div class="grid-item" @click="onFamily">\n            <div class="grid-icon purple-bg">🎨</div>',
    '          <div class="grid-item" @click="onPortalActivity">\n            <div class="grid-icon purple-bg">🎨</div>'
  )
  .replace(
    '          <div class="grid-item" @click="onHealthReport">\n            <div class="grid-icon orange-bg">📚</div>',
    '          <div class="grid-item" @click="onPortalCourse">\n            <div class="grid-icon orange-bg">📚</div>'
  )
  .replace(
    '          <div class="grid-item" @click="onHealthReport">\n            <div class="grid-icon blue-light-bg">📰</div>',
    '          <div class="grid-item" @click="onPortalNews">\n            <div class="grid-icon blue-light-bg">📰</div>'
  );

const portalSection = `
      <div v-if="portalMode" class="portal-section">
        <div class="portal-section-head" @click="portalSectionOpen = !portalSectionOpen">
          <span class="portal-section-title">{{ portalSectionTitle }}</span>
          <span class="portal-section-toggle">{{ portalSectionOpen ? "收起" : "展开" }}</span>
        </div>
        <div v-show="portalSectionOpen" class="portal-list">
          <motion.div v-for="item in portalListItems" :key="item.id" class="portal-card" @click="openPortalItem(item)">
            <div class="portal-card-thumb">
              <img v-if="item.imageUrl" :src="resolveMediaUrl(item.imageUrl)" :alt="item.title" />
              <span v-else class="portal-card-emoji">{{ item.icon }}</span>
            </div>
            <div class="portal-card-body">
              <div class="portal-card-title">{{ item.title }}</div>
              <div v-if="item.subtitle" class="portal-card-sub">{{ item.subtitle }}</div>
            </div>
          </div>
          <div v-if="portalListItems.length === 0" class="portal-empty">暂无内容</div>
        </div>
      </div>`;

template = template.replace(
  "      </motion.div>\n    </div>\n\n    <!-- ============================================================ -->\n    <!-- ===== 健康 TAB ===== -->",
  `      </div>${portalSection}\n    </div>\n\n    <!-- ============================================================ -->\n    <!-- ===== 健康 TAB ===== -->`
);

// fix hot-section close if above didn't match
if (!template.includes("portal-section")) {
  template = template.replace(
    "      </div>\n    </div>\n\n    <!-- ============================================================ -->\n    <!-- ===== 健康 TAB ===== -->",
    `      </motion.div>${portalSection}\n    </div>\n\n    <!-- ============================================================ -->\n    <!-- ===== 健康 TAB ===== -->`
  );
}

template = template.replace(
  '<div class="health-action-btn" @click="onSOS">\n            <span class="icon">🔔</span> 健康告警',
  '<div class="health-action-btn" @click="onHealthAlarms">\n            <span class="icon">🔔</span> 健康告警'
);

const extraVitals = `          <div class="form-row">
            <div class="form-label">体重 (kg)</div>
            <input v-model.number="vitalsForm.weight" type="number" class="wide-input" />
          </div>
          <div class="form-row">
            <div class="form-label">身高 (cm)</div>
            <input v-model.number="vitalsForm.height" type="number" class="wide-input" @input="updateBmi" />
          </div>
          <div class="form-row">
            <div class="form-label">BMI</div>
            <div class="step-value bmi-readonly">{{ vitalsForm.bmi || "--" }}</div>
          </div>
          <div class="form-row">
            <div class="form-label">体温 (℃)</div>
            <input v-model.number="vitalsForm.temperature" type="number" step="0.1" class="wide-input" />
          </div>
`;

template = template.replace(
  '          <div class="form-row">\n            <div class="form-label">血糖 (mmol/L)</div>',
  extraVitals + '          <div class="form-row">\n            <div class="form-label">血糖 (mmol/L)</div>'
);

const familyAndPortal = fs.readFileSync(path.join(__dirname, "elder-home-tail.html"), "utf8");
template = template.replace("    <!-- Toast 提示 -->", familyAndPortal + "\n    <!-- Toast 提示 -->");

// strip any motion tags
template = template.replace(/<\/?motion[^>]*>/g, (tag) => {
  if (tag.startsWith("</")) return tag.includes("motion.div") ? "</div>" : "";
  if (tag.includes("motion.div")) return "<div";
  return "";
});

const script = fs.readFileSync(path.join(__dirname, "elder-home-script.ts"), "utf8");
const extraStyles = fs.readFileSync(path.join(__dirname, "elder-home-extra.css"), "utf8");
styles = styles.replace("</style>", extraStyles + "\n</style>");

const out = `${template}\n\n<script setup lang="ts">\n${script}\n</script>\n\n${styles}\n`;
const outPath = path.join(root, "src/views/elder/ElderHome.vue");
fs.writeFileSync(outPath, out, "utf8");
console.log("OK", outPath, out.length);
