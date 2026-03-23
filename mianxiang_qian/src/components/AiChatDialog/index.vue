<template>
  <el-dialog
    :visible.sync="visible"
    :show-close="true"
    title=""
    width="560px"
    top="6vh"
    append-to-body
    custom-class="ai-chat-dialog"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div slot="title" class="ai-chat-header">
      <div class="ai-chat-header-left">
        <span class="ai-chat-title">
          <i class="el-icon-cpu" />
          AI 学习助手
        </span>
        <span v-if="chapterContext" class="ai-chat-context">{{ chapterContext }}</span>
      </div>
    </div>
    <div class="ai-chat-body">
      <div ref="messageList" class="ai-chat-messages">
        <div v-if="messages.length === 0" class="ai-chat-empty">
          <p>有什么想问的？输入问题，我会结合当前章节为你解答。</p>
        </div>
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['ai-chat-msg', msg.role === 'user' ? 'ai-chat-msg--user' : 'ai-chat-msg--assistant']"
        >
          <div class="ai-chat-msg-avatar">
            <i v-if="msg.role === 'user'" class="el-icon-user" />
            <i v-else class="el-icon-cpu" />
          </div>
          <div class="ai-chat-msg-content">
            <template v-if="msg.role === 'assistant'">
              <div class="ai-chat-markdown" v-html="renderMarkdown(msg.content)"></div>
              <span v-if="msg.streaming" class="ai-chat-cursor">|</span>
            </template>
            <template v-else>
              <span class="ai-chat-msg-text">{{ msg.content }}</span>
            </template>
          </div>
        </div>
      </div>
      <div class="ai-chat-input-wrap">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题…"
          :disabled="streaming"
          resize="none"
          class="ai-chat-input"
          @keydown.enter.native.prevent="handleEnter"
        />
        <el-button
          type="primary"
          :loading="streaming"
          :disabled="!inputText.trim()"
          class="ai-chat-send"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import MarkdownIt from 'markdown-it'
import { streamAiChat } from '@/api/student'

const md = new MarkdownIt({ html: false, linkify: true, typographer: true })

/** 预处理：AI 流式常无换行、# 后无空格、** 旁有空格，导致 markdown-it 不识别 */
function normalizeMarkdownInput(text) {
  if (!text || typeof text !== 'string') return ''
  return text
    // 在块级语法前补换行
    .replace(/([^\n])(#{1,6})(?=[^\n]|$)/g, '$1\n$2')  // "——###主要" -> "——\n###主要"
    .replace(/([^\n])```/g, '$1\n```')
    .replace(/```(\w*)([^\n\r])/g, '```$1\n$2')
    // markdown-it 要求 # 后必须有空格才识别为标题
    .replace(/(^|\n)(#{1,6})([^\s\n\r])/gm, '$1$2 $3')  // "###主要" -> "### 主要"
    // ** 与内容之间的空格会导致加粗不识别，去掉 ** 紧邻的空格
    .replace(/\*\*\s+/g, '**')
    .replace(/\s+\*\*/g, '**')
}

export default {
  name: 'AiChatDialog',
  props: {
    value: { type: Boolean, default: false },
    chapterContext: { type: String, default: '' }
  },
  data() {
    return {
      inputText: '',
      messages: [],
      streaming: false
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(v) {
        this.$emit('input', v)
      }
    }
  },
  watch: {
    value(v) {
      if (v && this.messages.length === 0) {
        this.$nextTick(() => this.scrollToBottom())
      }
    },
    messages: {
      handler() {
        this.$nextTick(() => this.scrollToBottom())
      },
      deep: true
    }
  },
  methods: {
    renderMarkdown(text) {
      if (!text || typeof text !== 'string') return ''
      const normalized = normalizeMarkdownInput(text)
      return md.render(normalized)
    },
    handleEnter() {
      if (!this.streaming && this.inputText.trim()) this.sendMessage()
    },
    scrollToBottom() {
      const el = this.$refs.messageList
      if (el) el.scrollTop = el.scrollHeight
    },
    sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.streaming) return
      this.inputText = ''
      this.messages.push({ role: 'user', content: text })
      this.messages.push({ role: 'assistant', content: '', streaming: true })
      this.streaming = true
      const lastIdx = this.messages.length - 1
      streamAiChat(
        {
          userContent: text,
          chapterContext: this.chapterContext || undefined
        },
        chunk => {
            const msg = this.messages[lastIdx]
            const nextContent = (msg.content || '') + chunk
            this.messages.splice(lastIdx, 1, { ...msg, content: nextContent })
          }
      )
        .then(() => {
          this.$set(this.messages[lastIdx], 'streaming', false)
        })
        .catch(err => {
          this.$set(this.messages[lastIdx], 'content', (this.messages[lastIdx].content || '') + '\n\n[回答出错：' + (err.message || '请稍后重试') + ']')
          this.$set(this.messages[lastIdx], 'streaming', false)
          this.$message.error('AI 回复失败，请重试')
        })
        .finally(() => {
          this.streaming = false
        })
    },
    handleClose() {
      this.$emit('close')
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-chat-header {
  padding: 4px 40px 4px 0;
  .ai-chat-header-left {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  .ai-chat-title {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 17px;
    font-weight: 700;
    color: #1a1a2e;
    i { font-size: 20px; color: #6366f1; }
  }
  .ai-chat-context {
    font-size: 12px;
    color: #94a3b8;
    max-width: 280px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.ai-chat-body {
  display: flex;
  flex-direction: column;
  min-height: 460px;
  max-height: 68vh;
}

.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  min-height: 340px;
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }
}

.ai-chat-empty {
  text-align: center;
  padding: 48px 24px;
  p {
    margin: 0;
    font-size: 14px;
    color: #94a3b8;
    line-height: 1.6;
  }
}

.ai-chat-msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
  &.ai-chat-msg--user {
    flex-direction: row-reverse;
    .ai-chat-msg-content {
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      color: #fff;
      border-radius: 16px 16px 4px 16px;
    }
    .ai-chat-msg-avatar {
      background: linear-gradient(135deg, #a5b4fc, #c4b5fd);
      color: #fff;
    }
  }
  &.ai-chat-msg--assistant .ai-chat-msg-content {
    background: #f1f5f9;
    color: #334155;
    border-radius: 16px 16px 16px 4px;
  }
}

.ai-chat-msg-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  color: #6366f1;
  font-size: 16px;
}

.ai-chat-msg-content {
  max-width: 85%;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  .ai-chat-msg-text { white-space: pre-wrap; }
}

.ai-chat-markdown {
  white-space: normal;
  ::v-deep {
    p { margin: 0 0 0.6em; &:last-child { margin-bottom: 0; } }
    h1, h2, h3, h4 { margin: 0.75em 0 0.4em; font-weight: 600; line-height: 1.3; }
    h1 { font-size: 1.15em; } h2 { font-size: 1.08em; } h3, h4 { font-size: 1em; }
    ul, ol { margin: 0.4em 0; padding-left: 1.4em; }
    li { margin: 0.2em 0; }
    code { background: rgba(0,0,0,0.06); padding: 0.15em 0.4em; border-radius: 4px; font-size: 0.92em; }
    pre { margin: 0.6em 0; padding: 10px 12px; border-radius: 8px; overflow-x: auto; background: rgba(0,0,0,0.06); }
    pre code { background: none; padding: 0; }
    blockquote { margin: 0.5em 0; padding-left: 12px; border-left: 3px solid rgba(99,102,241,0.5); color: #475569; }
    a { color: #6366f1; text-decoration: none; &:hover { text-decoration: underline; } }
    strong { font-weight: 600; }
    hr { margin: 0.8em 0; border: none; border-top: 1px solid rgba(0,0,0,0.08); }
  }
}

.ai-chat-cursor {
  display: inline-block;
  animation: blink 0.8s step-end infinite;
  color: #6366f1;
  margin-left: 2px;
}
@keyframes blink {
  50% { opacity: 0; }
}

.ai-chat-input-wrap {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  .ai-chat-input {
    flex: 1;
    ::v-deep textarea {
      border-radius: 12px;
      resize: none;
      font-size: 14px;
    }
  }
  .ai-chat-send {
    flex-shrink: 0;
    border-radius: 10px;
    padding: 10px 18px;
    font-weight: 500;
  }
}
</style>

<style lang="scss">
.ai-chat-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  .el-dialog__header {
    padding: 16px 24px 12px;
    border-bottom: 1px solid #e2e8f0;
    background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
  }
  .el-dialog__body {
    padding: 0 24px 24px;
    background: #fff;
  }
  /* v-html 插入的 markdown 内容无 scoped 属性，在此统一写样式确保生效 */
  .ai-chat-markdown {
    white-space: normal;
    word-break: break-word;
    p { margin: 0 0 0.6em; &:last-child { margin-bottom: 0; } }
    h1, h2, h3, h4, h5, h6 { margin: 0.75em 0 0.4em; font-weight: 600; line-height: 1.3; }
    h1 { font-size: 1.15em; } h2 { font-size: 1.08em; } h3, h4, h5, h6 { font-size: 1em; }
    ul, ol { margin: 0.4em 0; padding-left: 1.4em; }
    li { margin: 0.2em 0; }
    code { background: rgba(0,0,0,0.08); padding: 0.15em 0.4em; border-radius: 4px; font-size: 0.92em; font-family: Consolas, Monaco, monospace; }
    pre { margin: 0.6em 0; padding: 10px 12px; border-radius: 8px; overflow-x: auto; background: rgba(0,0,0,0.06); }
    pre code { background: none; padding: 0; }
    blockquote { margin: 0.5em 0; padding-left: 12px; border-left: 3px solid rgba(99,102,241,0.5); color: #475569; }
    a { color: #6366f1; text-decoration: none; &:hover { text-decoration: underline; } }
    strong { font-weight: 600; }
    hr { margin: 0.8em 0; border: none; border-top: 1px solid rgba(0,0,0,0.08); }
  }
}
</style>
