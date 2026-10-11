import io

# GearCatalogItem: searchTerms
p = "src/main/java/com/ty/hikingalone/domain/gear/entity/GearCatalogItem.java"
src = io.open(p, encoding="utf-8").read()
old = "    private String brand;\n\n    /**\n     * 典型自重 克（可空）\n     */"
new = "    private String brand;\n\n    /**\n     * 联想别名（中文品牌名/拼音/型号缩写，空格分隔，可空）\n     */\n    private String searchTerms;\n\n    /**\n     * 典型自重 克（可空）\n     */"
assert src.count(old) == 1, "entity field"
src = src.replace(old, new)
old = "    public static GearCatalogItem reconstruct(Long id, String name, Integer category, String brand,\n                                              Integer weightGrams, Integer sort) {\n        GearCatalogItem item = new GearCatalogItem();\n        item.id = id;\n        item.name = name;\n        item.category = category;\n        item.brand = brand;\n        item.weightGrams = weightGrams;"
new = "    public static GearCatalogItem reconstruct(Long id, String name, Integer category, String brand,\n                                              String searchTerms, Integer weightGrams, Integer sort) {\n        GearCatalogItem item = new GearCatalogItem();\n        item.id = id;\n        item.name = name;\n        item.category = category;\n        item.brand = brand;\n        item.searchTerms = searchTerms;\n        item.weightGrams = weightGrams;"
assert src.count(old) == 1, "entity reconstruct"
src = src.replace(old, new)
io.open(p, "w", encoding="utf-8", newline="\n").write(src)

# PO
p = "src/main/java/com/ty/hikingalone/infrastructure/gear/po/GearCatalogPO.java"
src = io.open(p, encoding="utf-8").read()
old = "    /**\n     * 品牌（型号级条目，可空）\n     */\n    private String brand;"
new = "    /**\n     * 品牌（型号级条目，可空）\n     */\n    private String brand;\n\n    /**\n     * 联想别名（可空）\n     */\n    private String searchTerms;"
assert src.count(old) == 1, "po"
src = src.replace(old, new)
io.open(p, "w", encoding="utf-8", newline="\n").write(src)

# repo impl: reconstruct + normalized search
p = "src/main/java/com/ty/hikingalone/infrastructure/gear/repository/impl/GearCatalogRepositoryImpl.java"
src = io.open(p, encoding="utf-8").read()
old = "        return GearCatalogItem.reconstruct(\n                po.getId(), po.getName(), po.getCategory(), po.getBrand(), po.getWeightGrams(), po.getSort());"
new = "        return GearCatalogItem.reconstruct(\n                po.getId(), po.getName(), po.getCategory(), po.getBrand(), po.getSearchTerms(),\n                po.getWeightGrams(), po.getSort());"
assert src.count(old) == 1, "impl reconstruct"
src = src.replace(old, new)
old = """        String k = keyword.trim();
        String kLower = k.toLowerCase();
        // 匹配名称或品牌（大小写不敏感，「gregory」命中品牌列）；前缀命中优先，同档按目录顺序
        Comparator<GearCatalogItem> byPrefixFirst = Comparator.comparingInt(
                item -> item.getName().toLowerCase().startsWith(kLower) ? 0 : 1);
        return all.stream()
                .filter(item -> item.getName().toLowerCase().contains(kLower)
                        || (item.getBrand() != null && item.getBrand().toLowerCase().contains(kLower)))
                .sorted(byPrefixFirst.thenComparing(GearCatalogItem::getSort)
                        .thenComparing(GearCatalogItem::getId))
                .limit(limit)
                .toList();"""
new = """        // 归一化匹配：去所有空白 + 小写（「格里高利B65」=「格里高利 B65」，名称/品牌/中文别名都命中）
        String k = normalize(keyword.trim());
        Comparator<GearCatalogItem> byPrefixFirst = Comparator.comparingInt(
                item -> normalize(item.getName()).startsWith(k) ? 0 : 1);
        return all.stream()
                .filter(item -> normalize(item.getName()).contains(k)
                        || (item.getBrand() != null && normalize(item.getBrand()).contains(k))
                        || (item.getSearchTerms() != null && normalize(item.getSearchTerms()).contains(k)))
                .sorted(byPrefixFirst.thenComparing(GearCatalogItem::getSort)
                        .thenComparing(GearCatalogItem::getId))
                .limit(limit)
                .toList();"""
assert src.count(old) == 1, "impl search"
src = src.replace(old, new)
old = "    private GearCatalogItem toEntity(GearCatalogPO po) {"
new = "    /** 归一化：去所有空白 + 小写（中文输入习惯性不加空格，数据库里带空格） */\n    private String normalize(String s) {\n        return s == null ? \"\" : s.replaceAll(\"\\\\s+\", \"\").toLowerCase();\n    }\n\n    private GearCatalogItem toEntity(GearCatalogPO po) {"
assert src.count(old) == 1, "impl normalize"
src = src.replace(old, new)
io.open(p, "w", encoding="utf-8", newline="\n").write(src)

# VO
p = "src/main/java/com/ty/hikingalone/interfaces/gear/vo/query/GearCatalogVO.java"
src = io.open(p, encoding="utf-8").read()
old = "    /** 品牌（型号级条目，可空） */\n    private String brand;"
new = "    /** 品牌（型号级条目，可空） */\n    private String brand;\n\n    /** 联想别名（可空） */\n    private String searchTerms;"
assert src.count(old) == 1, "vo field"
src = src.replace(old, new)
old = "        vo.setBrand(item.getBrand());"
new = "        vo.setBrand(item.getBrand());\n        vo.setSearchTerms(item.getSearchTerms());"
assert src.count(old) == 1, "vo of"
src = src.replace(old, new)
io.open(p, "w", encoding="utf-8", newline="\n").write(src)
print("catalog searchTerms patched")
