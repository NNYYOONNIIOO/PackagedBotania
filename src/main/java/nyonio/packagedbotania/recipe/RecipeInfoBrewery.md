# RecipeInfoBrewery 实现逻辑

## 概述
`RecipeInfoBrewery` 是植物酿造台配方信息的实现类，负责存储、匹配、序列化和编码酿造配方。

## 设计思路

### 数据模型
- `inputs`: 原料列表（不含酿造容器），仅包含 `RecipeBrew.getInputs()` 中的 ItemStack 类型条目
- `output`: 酿造产物，通过 `RecipeBrew.getOutput(brewContainer)` 计算得到
- `brewContainer`: 酿造容器（IBrewContainer），如药水瓶或酒壶
- `recipe`: 原始 Botania `RecipeBrew` 引用
- `manaCost`: 魔力消耗，由 `IBrewContainer.getManaCost(recipe.getBrew(), brewContainer)` 计算

### 核心流程

#### 1. setRecipe() — 配方设置
- 保存 recipe 和 brewContainer
- 从 `recipe.getInputs()` 中提取 ItemStack 类型条目作为 inputs（忽略 ore dictionary 字符串，因为编码系统只处理具体物品）
- 通过 `recipe.getOutput(brewContainer)` 计算输出
- 通过 `IBrewContainer.getManaCost(recipe.getBrew(), brewContainer)` 计算魔力消耗

#### 2. generateFromStacks() — 从编码器物品栏生成配方
- 清空所有字段
- 遍历 `RecipeTypeBrewery.SLOTS` 对应的编码槽位
- 第一个遇到的 `IBrewContainer` 物品保存为 brewContainer，其余物品收集为 collectedInputs
- 遍历 `BotaniaAPI.brewRecipes` 寻找匹配的 `RecipeBrew`
- 调用 `matchesRecipe()` 进行原料匹配
- 找到匹配后通过 `setRecipe()` 设置配方（注意：setRecipe 会重新从 recipe.getInputs() 生成 inputs，随后被覆盖为 collectedInputs 以保留原始物品栈）

#### 3. matchesRecipe() — 原料匹配
- 与 Apothecary 相同的匹配算法
- 逐一将输入物品与配方原料列表进行匹配
- 支持 ItemStack 精确匹配和 Ore Dictionary 字符串匹配
- 使用"消耗式"匹配：匹配成功后从配方列表中移除该条目
- 所有输入匹配完毕后，配方列表应为空

#### 4. getEncoderStacks() — 编码器物品映射
- 将 brewContainer 映射到第一个编码槽位
- 将原料依次映射到后续编码槽位

#### 5. NBT 序列化
- 保存 "Inputs"、"Output"、"BrewContainer" 三个 NBT 标签
- 读取时通过 `hasKey("id")` 检查 ItemStack 是否有效

### PackagePatternBrewery 内部类
- `getInputs()`: 返回酿造容器 + 原料的完整列表（用于封装包模式）
- `getOutput()`: 调用 `ItemPackage.makePackage()` 生成封装包物品栈

## 与 Apothecary 的对比
- Apothecary 只有一个"催化剂"槽位，而 Brewery 需要一个专门的 IBrewContainer 槽位
- Brewery 的 output 依赖于 brewContainer（不同容器产生不同输出），而 Apothecary 的 output 是固定的
- Brewery 最多有 6 个原料槽位（Apothecary 的花瓣/原料槽位数量不同）
