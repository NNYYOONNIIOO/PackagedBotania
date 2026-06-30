# TileBreweryCrafter 实现逻辑

## 概述
`TileBreweryCrafter` 是封包植物酿造台的方块实体类，继承 `TileAE2Base`，实现 `IPackageCraftingMachine` 和 `ISparkAttachable` 接口。与 Rune Altar / Terra Plate 一致，采用**魔力驱动合成进度 + FE 能量维持机器运转**的双能量模式。

## 设计思路

### 物品栏布局（18 槽位，与其他机器一致）
| 槽位 | 用途 |
|------|------|
| 0-6  | 输入（0=酿造容器，1-6=原料） |
| 7-15 | 保留（未使用） |
| 16   | 能量物品（Forge Energy 兼容） |
| 17   | 输出槽 |

### 双能量系统

#### FE 能量（机器运转）
- **energyCapacity**: 5000 FE
- **energyUsage**: 100 FE/tick
- 作用：维持机器通电状态，每 tick 从内部存储或 AE2 网络提取 `energyUsage` 点能量
- 能量不足时机器暂停（不消耗魔力，不推进进度）
- 充电：优先 AE2 网络 → 内部能量存储 → slot 16 的 FE 物品

#### 魔力（合成进度）
- 通过 **火花（Spark）** 从附近魔力池主动拉取
- 实现 `ISparkAttachable` 接口支持火花附着
- 魔力接收方式：`requestManaFromSparks()` 每 tick 寻找附近的火花 → 魔力池组合，注册传输
- `IManaReceiver.recieveMana()` 接收魔力脉冲
- 魔力容量 = `totalManaCost`（由 `IBrewContainer.getManaCost()` 决定）
- 魔力满时自动完成合成

### 工作流程

#### 1. 接收封装包 — acceptPackage()
- 检查：非忙碌 + 配方有效 + 类型为 IRecipeInfoBrewery
- 设置 `currentRecipe`、`totalManaCost = recipe.getMana()`
- 标记 `isWorking = true`
- 将封装包物品填入 slot 0-6

#### 2. 每 tick 更新 — update()
- 首 tick 执行 `onReady()`（AE2 网络初始化）
- 充电 `chargeEnergy()`
- 工作中且能量充足时：
  - 消耗 FE 能量维持机器运转
  - 调用 `requestManaFromSparks()` 从附近魔力池拉取魔力
- 魔力满 (`mana >= totalManaCost`) → `finishProcess()` → `ejectItems()`
- 每 8 tick 尝试输出

#### 3. 完成合成 — finishProcess()
- 清空输入槽 0-6 和能量槽 16
- 将 `currentRecipe.getOutput()` 放入输出槽 17
- 播放 `ModSounds.potionCreate` 音效
- 调用 `endProcess()` 重置所有状态

### NBT 持久化
- **保存**: Working、Mana、TotalManaCost、Recipe
- **读取**: 恢复所有字段
- **同步 NBT**: Mana、Working（客户端需显示魔力条）

### 进度显示
- `getScaledEnergy()`: 能量条缩放
- `getScaledMana()`: 魔力条缩放（基于 totalManaCost）

### ISparkAttachable 接口方法
- `canAttachSpark()`: 始终返回 true
- `getAttachedSpark()`: 检测方块上方 1 格范围内的火花实体
- `areIncomingTranfersDone()`: 魔力满时返回 true
- `getAvailableSpaceForMana()`: 返回剩余魔力容量

### IManaReceiver 接口方法
- `getCurrentMana()`: 返回当前魔力（仅工作中）
- `isFull()`: 魔力满时返回 true
- `recieveMana()`: 接收魔力，更新魔力条，触发客户端同步
- `canRecieveManaFromBursts()`: 工作中且未满时返回 true

## 与 Rune Altar / Terra Plate 的对比
| 特性 | Brewery | Rune Altar | Terra Plate |
|------|---------|-----------|-------------|
| 魔力供给 | IBrewContainer.getManaCost() | RecipeRuneAltar.getMana() | RecipeTerraPlate.getMana() |
| 输入槽位 | 7（1 容器 + 6 原料） | 最多 17 | 最多 16 |
| 音效 | ModSounds.potionCreate | ModSounds.runeAltarCraft | ModSounds.terrasteelCraft |
| 额外逻辑 | 无 | 符文回收 | 多方块结构替换 |

## 与 Botania 原版酿造台的对比
- **原版**: 实现 `IManaReceiver`，直接接收魔力脉冲（无火花机制）
- **封包版**: 实现 `ISparkAttachable`，通过火花网络从魔力池主动拉取
- **原版**: 手动放入/取出物品
- **封包版**: 通过封装包自动输入，AE2 网络自动输出
