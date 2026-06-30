# BlockBreweryCrafter 实现逻辑

## 概述
`BlockBreweryCrafter` 是封包植物酿造台的方块类，定义了方块的基本属性和注册信息。

## 设计思路

### 方块属性
- **Material.ROCK**: 与其他 Crafter 方块一致，表示石质机器
- **Hardness 3.0F / Resistance 5.0F**: 标准机器方块硬度和爆炸抗性
- **AABB (0,0,0,1,1,1)**: 完整的单格碰撞箱，与原版酿造台不同（原版酿造台使用 TESR 渲染，碰撞箱较小）

### 单例模式
遵循项目统一模式：
- `INSTANCE`: 方块单例
- `ITEM_INSTANCE`: ItemBlock 单例，注册名为 `packaged_botania:brewery_crafter`
- `MODEL_LOCATION`: 模型资源位置 `packaged_botania:brewery_crafter#normal`

### 与其他 Crafter 方块的对比
- **ApothecaryCrafter**: 2 格高 AABB，额外有 ApothecaryCrafterPart 多方块部件
- **BreweryCrafter**: 1×1×1 完整方块，无需多方块结构
  - 原版 Botania 酿造台虽然外观较小（中心柱+旋转托盘），但作为封装机器使用完整方块更合理
  - 简化了碰撞和交互逻辑

### TileEntity 关联
- `createNewTileEntity()` 返回 `TileBreweryCrafter` 实例

### 模型注册
- `registerModels()` 注册物品模型，映射到 `MODEL_LOCATION`
