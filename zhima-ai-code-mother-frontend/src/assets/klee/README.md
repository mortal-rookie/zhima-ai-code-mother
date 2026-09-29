# 可莉互动精灵

`klee-sprites.png` 是使用内置 image_gen 生成的《原神》可莉同人插画，透明 RGBA 四格精灵图，非官方游戏素材。网页用 CSS 的 200% 背景尺寸显示单格：左上待机、右上眨眼、左下挥手、右下欢呼。无远程图片请求或额外动画库。

使用处：`src/components/KleeCompanion.vue`，创作首页的问候区。支持悬停／键盘聚焦挥手、点击跳跃与气泡、隐藏与重新召唤。离开视口／隐藏标签页时暂停，遵循减少动态效果设置。

## 生成提示词（原文）

Use case: stylized-concept. Asset type: a production 2x2 sprite sheet for a tiny interactive website mascot. Primary request: Klee (可莉) from Genshin Impact, recognizable faithful cute chibi fan illustration: red beret with white clover insignia and white feather, pale blonde twin pigtails, pointy elf ears, red eyes, red coat with cream trim, small brown backpack, brown boots. Childlike innocent cheerful proportions. Style: polished soft hand-painted anime chibi, clean edges, subtle pastel shading, tiny warm blush, gently muted red compatible with a warm ivory and mist-blue glass website. Background MUST be fully transparent alpha, no checkerboard painted, no stage or floor. Composition MUST be exactly a square 1024x1024 canvas partitioned into FOUR EQUAL 512x512 cells, 2 columns and 2 rows, no visible dividers. One complete full-body Klee centered in each cell. Identical face, outfit, body proportions, scale and foot baseline for all 4 frames. Every figure stays safely within its cell, no overlap. Each figure occupies roughly 80 percent of cell height with head at y55 and foot baseline y460 within its own cell. Top-left frame: relaxed standing, eyes open, arms low, sweet smile. Top-right frame: identical relaxed standing with eyes closed in a gentle blink. Bottom-left frame: eyes open and one hand raised waving next to her head, cheerful greeting. Bottom-right frame: both hands lifted with joyful expression, ready to hop, keep feet baseline same. No labels, no words, no watermark, no decorative sparkles around the characters, no bombs, no weapons. The cells will be displayed separately via CSS background-position, so exact alignment and consistency are crucial.

生成图实际为 1254×1254，CSS 按比例分格，无需裁剪。
