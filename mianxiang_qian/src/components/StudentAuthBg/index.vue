<template>
  <div class="student-auth-bg">
    <div class="gradient-bg" />
    <div class="balls">
      <div class="ball ball-1" />
      <div class="ball ball-2" />
      <div class="ball ball-3" />
      <div class="ball ball-4" />
      <div class="ball ball-5" />
      <div class="ball ball-6" />
      <div class="ball ball-7" />
    </div>
    <div class="content">
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentAuthBg'
}
</script>

<style lang="scss" scoped>
.student-auth-bg {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
  max-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gradient-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 25%, #91caff 50%, #69b1ff 75%, #4096ff 100%);
  background-size: 200% 200%;
  animation: gradientShift 12s ease infinite;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.balls {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.ball {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(1px);
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
}

.ball-1 {
  width: 80px;
  height: 80px;
  left: 10%;
  top: 15%;
  animation: float1 18s infinite;
}
.ball-2 {
  width: 120px;
  height: 120px;
  right: 12%;
  top: 25%;
  animation: float2 22s infinite;
}
.ball-3 {
  width: 60px;
  height: 60px;
  left: 25%;
  bottom: 20%;
  animation: float3 15s infinite;
}
.ball-4 {
  width: 100px;
  height: 100px;
  right: 25%;
  bottom: 15%;
  animation: float4 20s infinite;
}
.ball-5 {
  width: 50px;
  height: 50px;
  left: 50%;
  top: 10%;
  animation: float5 16s infinite;
}
.ball-6 {
  width: 70px;
  height: 70px;
  right: 8%;
  bottom: 35%;
  animation: float6 19s infinite;
}
.ball-7 {
  width: 90px;
  height: 90px;
  left: 8%;
  top: 45%;
  animation: float7 21s infinite;
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -40px) scale(1.05); }
  50% { transform: translate(-20px, 30px) scale(0.95); }
  75% { transform: translate(40px, 20px) scale(1.02); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(-50px, -30px); }
  66% { transform: translate(20px, 50px); }
}

@keyframes float3 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(40px, -35px); }
}

@keyframes float4 {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(-30px, -25px); }
  75% { transform: translate(25px, 40px); }
}

@keyframes float5 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-35px, 45px); }
}

@keyframes float6 {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(45px, -20px); }
  66% { transform: translate(-25px, -45px); }
}

@keyframes float7 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-40px, 25px); }
}

.content {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}
</style>
