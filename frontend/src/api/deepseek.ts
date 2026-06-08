/**
 * DeepSeek Chat API（OpenAI 兼容）。
 * 请求发到 `/api/deepseek/v1/chat/completions`，由后端转发至 DeepSeek；密钥配置在后端环境变量 DEEPSEEK_API_KEY 或 application.yml。
 */

const CHAT_PATH = "/api/deepseek/v1/chat/completions";

const SYSTEM_PROMPT = `你是「银发智盾」智慧养老演示系统首页里的智能助手，工作方式类似浏览器搜索框：用户输入任何问题，你都要尽量直接、准确地用中文作答（常识、百科、操作步骤、名词解释等均可）。

当问题与本演示系统相关时，在回答中顺带说明：各端入口为长者端（体征、SOS）、家属端（告警、地图）、服务机构（派单、工单）、管理端（大屏、报表）；演示登录使用注册时分配的 E/C/G/A 前缀账号。不要编造不存在的菜单路径。

回答简洁有层次，先给结论再补充要点；默认约 400 字以内，用户明确要求长文或细节时可更长。`;

export type ChatTurn = { role: "user" | "assistant"; text: string };

/** 去掉开头的 assistant 轮次，避免违反「system 之后应先出现 user」的常见约定，并排除仅用于展示的欢迎语。 */
function turnsForApi(turns: ChatTurn[]): ChatTurn[] {
  let i = 0;
  while (i < turns.length && turns[i].role === "assistant") i += 1;
  return turns.slice(i);
}

type DeepSeekResponse = {
  choices?: Array<{ message?: { content?: string | null } }>;
  error?: { message?: string };
};

/** 将常见英文 API 错误转成用户可理解的中文说明 */
function humanizeApiError(message: string): string {
  const m = message.trim();
  if (/insufficient\s+balance/i.test(m)) {
    return (
      "DeepSeek 提示「余额不足」：接口已打通，但当前账号没有可用额度。请到 https://platform.deepseek.com 登录后充值或查看账单/赠送额度，再重试提问。"
    );
  }
  if (/invalid|incorrect|api\s*key|authentication|unauthorized|401/i.test(m)) {
    return `密钥无效或未通过校验：${m}。请在 backend 的 local-deepseek.yml 或环境变量 DEEPSEEK_API_KEY 中填写平台生成的有效密钥，并重启后端。`;
  }
  if (/rate\s*limit|429|too\s*many\s*requests/i.test(m)) {
    return `请求过于频繁，请稍后再试。（平台原文：${m}）`;
  }
  return m;
}

export async function deepSeekChat(turns: ChatTurn[]): Promise<string> {
  const apiTurns = turnsForApi(turns);
  if (apiTurns.length === 0 || apiTurns.every((t) => t.role !== "user")) {
    throw new Error("请先输入一个问题");
  }

  const messages: Array<{ role: "system" | "user" | "assistant"; content: string }> = [
    { role: "system", content: SYSTEM_PROMPT },
    ...apiTurns.map((t) => ({ role: t.role, content: t.text }))
  ];

  let res: Response;
  try {
    res = await fetch(CHAT_PATH, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        model: "deepseek-chat",
        messages,
        max_tokens: 2048,
        temperature: 0.4,
        stream: false
      })
    });
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    if (/failed to fetch|networkerror|load failed|network request failed/i.test(msg)) {
      throw new Error(
        "无法连接后端：智慧助手请求会经前端 dev 转发到 http://localhost:8081。请先在项目的 backend 目录运行 .\\mvnw.cmd spring-boot:run（或 IDE 启动 ElderApplication），确认本机 8081 已监听后再提问。"
      );
    }
    throw e;
  }

  const raw = await res.text();
  let data: DeepSeekResponse;
  try {
    data = JSON.parse(raw) as DeepSeekResponse;
  } catch {
    throw new Error(raw.slice(0, 200) || res.statusText);
  }

  if (!res.ok) {
    const rawMsg = data.error?.message || raw.slice(0, 200) || res.statusText;
    throw new Error(humanizeApiError(rawMsg));
  }

  if (data.error?.message && !data.choices?.length) {
    throw new Error(humanizeApiError(data.error.message));
  }

  const content = data.choices?.[0]?.message?.content?.trim();
  if (!content) throw new Error("模型未返回有效内容");
  return content;
}
