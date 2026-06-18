# IRecipeInfoBrewery 接口实现逻辑

## 概述
`IRecipeInfoBrewery` 是植物酿造台（Brewery）配方信息接口，扩展自 PackagedAuto 的 `IRecipeInfo` 接口，定义了酿造配方所需的数据访问方法。

## 设计思路

### 接口继承
继承 `IRecipeInfo` 以兼容 PackagedAuto 的配方编码系统，同时添加酿造特有的方法。

### 核心方法
- **`getInputs()`**: 返回原料列表（不含酿造容器），用于配方匹配和编码
- **`getOutput()`**: 返回酿造产物（已应用酿造容器后的结果 ItemStack）
- **`getBrewContainer()`**: 返回酿造容器（IBrewContainer，如药水瓶/酒壶），这是 Brewery 区别于其他合成台的关键——需要容器才能确定输出
- **`getRecipe()`**: 返回原始 Botania `RecipeBrew` 对象，用于配方验证
- **`getMana()`**: 返回酿造所需的魔力消耗量，由 `IBrewContainer.getManaCost(brew, stack)` 计算

### 默认方法
- **`getOutputs()`**: 覆盖 `IRecipeInfo` 的默认实现，将单个 output 包装为列表返回。如果 output 为空则返回空列表

## 与 Botania API 的关系
Botania 的 `RecipeBrew.getOutput(ItemStack)` 需要传入 IBrewContainer 的 ItemStack 才能计算输出，因此必须单独保存 brewContainer 信息，而不能仅依赖 inputs 列表。
