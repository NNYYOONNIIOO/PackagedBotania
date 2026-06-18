# RecipeTypeBrewery 实现逻辑

## 概述
`RecipeTypeBrewery` 定义了植物酿造台的配方类型，负责编码槽位布局、JEI 集成和视觉表示。

## 设计思路

### 编码槽位布局（SLOTS）
在 PackagedAuto 编码器的 9×9 物品栏中，Brewery 使用以下 7 个槽位：
- **槽位 0** (`9*1+2 = 11`): 酿造容器（IBrewContainer，如药水瓶/酒壶）
- **槽位 1-6** (`9*(row+2)+(col+3)`, 两行三列): 原料

布局在编码器中的可视化（第2-4行，第3-5列区域）：
```
行1: ...  C  ... ... ...   (C = 容器, 在行1列2)
行2: ... I1  I2  I3 ...   (I1-I3 = 第一行原料)
行3: ... I4  I5  I6 ...   (I4-I6 = 第二行原料)
```

### JEI 集成
- **CATEGORIES**: `"botania.brewery"` — 对应 Botania JEI 插件中的 Brewery 配方分类
- **getRecipeTransferMap()**: 从 JEI 配方布局中提取物品映射
  - 跳过 `ModBlocks.brewery` 显示项（JEI 中 brewery 方块作为背景/催化剂显示）
  - 最多映射 7 个槽位（1 容器 + 6 原料）
  - 按 JEI ingredient 遍历顺序填充编码槽位

### 视觉表示
- **getRepresentation()**: 使用 `ModBlocks.brewery` 方块物品作为配方类型的图标
- **COLOR**: 灰色 (139, 139, 139)，与酿造台金属质感匹配
- **COLOR_DISABLED**: 深灰色 (64, 64, 64)，用于禁用槽位
- **getSlotColor()**: 编码槽位和额外槽位 (slot 85) 使用启用色，其余使用禁用色

## 与其他 RecipeType 的对比
- Brewery 只有 7 个编码槽位（vs Apothecary 的可变数量）
- Brewery 需要特殊处理 IBrewContainer 槽位
- Brewery 的 JEI 转移需要跳过 brewery 方块显示项
