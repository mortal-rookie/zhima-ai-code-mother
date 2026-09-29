<script setup lang="ts">
defineProps<{ tone: string }>()
</script>
<template>
  <div class="template-art" :class="tone" aria-hidden="true">
    <template v-if="tone === 'violet'"
      ><i class="bar one"></i><i class="bar two"></i><i class="bar three"></i></template
    ><template v-else-if="tone === 'mint'"><i class="orb one"></i><i class="orb two"></i></template
    ><template v-else
      ><i class="tile one"></i><i class="tile two"><b></b><b></b><b></b></i
    ></template>
  </div>
</template>
<style scoped>
.template-art {
  height: 170px;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  background: radial-gradient(ellipse at 50% 90%, #dce8f0, transparent 65%), #f0f5f8;
}
i {
  display: block;
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 8px 16px 25px rgba(110, 149, 180, 0.15);
}
.tile {
  border-radius: 16px;
  background: linear-gradient(135deg, #9bceec, #dcecf5);
}
.tile.one {
  width: 78px;
  height: 96px;
  left: calc(50% - 50px);
  top: 30px;
}
.tile.two {
  width: 62px;
  height: 75px;
  top: 66px;
  left: calc(50% + 6px);
  background: linear-gradient(135deg, rgba(160, 205, 232, 0.75), rgba(240, 249, 253, 0.8));
  backdrop-filter: blur(8px);
  padding: 18px 10px;
}
b {
  display: block;
  height: 5px;
  width: 25px;
  border-radius: 5px;
  background: rgba(77, 137, 178, 0.22);
  margin-bottom: 8px;
}
b:nth-child(2) {
  width: 18px;
}
.peach {
  background: radial-gradient(ellipse at 50% 90%, #f7e6d3, transparent 65%), #fbf4eb;
}
.peach .tile.one {
  background: linear-gradient(135deg, #f6cc93, #fbe5c5);
}
.peach .tile.two {
  background: linear-gradient(135deg, rgba(249, 219, 178, 0.85), rgba(255, 247, 235, 0.7));
}
.peach b {
  background: rgba(210, 145, 65, 0.2);
}
.violet {
  background: radial-gradient(ellipse at 50% 95%, #e4def8, transparent 70%), #f4f1fb;
}
.bar {
  bottom: 30px;
  width: 30px;
  border-radius: 9px;
  background: linear-gradient(135deg, #c8bffd, #ece8fc);
}
.bar.one {
  height: 48px;
  left: calc(50% - 50px);
}
.bar.two {
  height: 75px;
  left: calc(50% - 12px);
}
.bar.three {
  height: 106px;
  left: calc(50% + 26px);
}
.mint {
  background: radial-gradient(ellipse at 50% 90%, #d9eae3, transparent 65%), #f1f7f4;
}
.orb {
  border-radius: 50%;
  background: radial-gradient(circle at 30% 20%, #b9e3d6, #83bfb0 60%, #d9efe8);
}
.orb.one {
  width: 85px;
  height: 85px;
  top: 35px;
  left: calc(50% - 52px);
}
.orb.two {
  width: 69px;
  height: 69px;
  top: 72px;
  left: calc(50% + 2px);
  background: radial-gradient(circle at 30% 20%, rgba(80, 164, 145, 0.6), rgba(184, 222, 209, 0.6));
  backdrop-filter: blur(7px);
}
</style>
