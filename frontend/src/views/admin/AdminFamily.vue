<template>
  <div class="admin-family">
    <h1 class="page-title">亲情互动 | 数字化关怀通道</h1>

    <div class="grid">
      <div class="card">
        <h2>家庭相册 & 动态分享</h2>
        <div class="feed">
          <div class="feed-meta">张爷爷的家属 · 10 分钟前</div>
          <div class="ph">相册预览（演示）</div>
          <p class="cap">重阳活动合影，祝父亲身体健康、开心每一天。</p>
          <div class="row-btns">
            <button type="button" class="b1">上传照片</button>
            <button type="button" class="b2">发送关怀提醒</button>
          </div>
        </div>
      </div>

      <div class="card">
        <h2>一键呼叫 & 紧急联系人</h2>
        <button type="button" class="sos">SOS 一键呼救</button>
        <p class="hint">女儿 138****1234 | 院内医务 120 转分机 3（演示）</p>
      </div>

      <div class="card wide">
        <h2>即时通讯（模拟）</h2>
        <div class="chat">
          <div class="bubble child"><span class="who">子女</span>爸爸，今天血压还好吗？<span class="t">10:24</span></div>
          <div class="bubble elder"><span class="who">老人</span>感觉挺好，谢谢你们关心～</div>
          <div class="bubble staff"><span class="who">护工</span>陈爷爷刚做完康复训练<span class="t">09:15</span></div>
        </div>
      </div>

      <div class="card">
        <h2>用药提醒 & 生日关怀</h2>
        <ul class="tl">
          <li class="dot blue">刘奶奶 · 降压药硝苯地平 · 今日 12:30</li>
          <li class="dot green">王爷爷 · 胰岛素注射提醒 · 明日 08:00</li>
          <li class="dot green">赵奶奶 · 86 岁生日 · 家属已预订蛋糕 · 3 月 20 日</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 1000;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
.card.wide {
  grid-column: 1 / -1;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.card h2 {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 1000;
}
.feed-meta {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 800;
  margin-bottom: 10px;
}
.ph {
  height: 120px;
  background: linear-gradient(135deg, #e8f4ff, #f5f5f5);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(0, 0, 0, 0.35);
  font-weight: 800;
  margin-bottom: 10px;
}
.cap {
  font-size: 14px;
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.65);
}
.row-btns {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.b1 {
  border: 0;
  background: #409eff;
  color: #fff;
  font-weight: 900;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
}
.b2 {
  border: 1px solid rgba(64, 158, 255, 0.5);
  background: #fff;
  color: #409eff;
  font-weight: 900;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
}
.sos {
  width: 100%;
  border: 0;
  background: linear-gradient(180deg, #f56c6c, #e53935);
  color: #fff;
  font-weight: 1000;
  font-size: 18px;
  padding: 18px;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(229, 57, 53, 0.35);
}
.hint {
  margin-top: 12px;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.5);
  font-weight: 700;
}
.chat {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bubble {
  max-width: 92%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.45;
  font-weight: 700;
}
.bubble .who {
  display: block;
  font-size: 11px;
  opacity: 0.65;
  margin-bottom: 4px;
}
.bubble .t {
  float: right;
  font-size: 11px;
  opacity: 0.5;
  margin-left: 8px;
}
.bubble.child {
  background: #ecf5ff;
  align-self: flex-start;
}
.bubble.elder {
  background: #f0f9eb;
  align-self: flex-end;
}
.bubble.staff {
  background: #fdf6ec;
  align-self: flex-start;
}
.tl {
  list-style: none;
  padding: 0;
  margin: 0;
}
.tl li {
  position: relative;
  padding-left: 18px;
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.72);
}
.tl li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 6px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.dot.blue::before {
  background: #409eff;
}
.dot.green::before {
  background: #67c23a;
}
</style>
