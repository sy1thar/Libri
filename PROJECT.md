# Libri — паспорт проекта (для контекста ИИ)

Fabric-мод, оверхоул зачарований и механик снаряжения.
**Актуальная ветка (`main`):** `1.7.0+26.3` | **MC:** `26.3` | **Java:** `25` | **Fabric Loader:** `≥0.20.1` | **Fabric API:** `0.166.0+26.3` | **Cloth Config:** `26.3.159` | **ModMenu:** `21.0.0` (опционально).
**MOD_ID:** `libri`. Серверно-клиентский мод (тултипы и локализация в `LibriClient`, экран наковальни `AnvilScreenMixin`).
**Ветки версий:** `main` (`26.3`) и `mc/<version>` (бэкпорты от `26.2` и `26.1.x` до `1.20.1`). Собранные JAR: `releases/` и `releases/legacy/`.

---

## Структура (`com.libri`)

- **`LibriMod`**: Точка входа `ModInitializer`. Регистрация `PARALYSIS_EFFECT_HOLDER` (`libri:paralysis`), блока и предмета `ADVANCED_ANVIL` / `ADVANCED_ANVIL_ITEM` (с `.setId(...)`), инициализация конфига, событий, таблиц лута и креативной вкладки.
  - Обработчик `ServerLivingEntityEvents.ALLOW_DAMAGE`: парализованная сущность не может наносить урон; стрела игрока с `Paralysis` на луке/арбалете (`mainHand`/`offHand`) накладывает паралич на 600 тиков (30 сек). Лог инициализации: `"Libri mod initialized with 14 enchantments"`.
- **`block/AdvancedAnvilBlock`**: Наследник `AnvilBlock`, открывающий наковальню с заголовком `container.libri.advanced_anvil` (всегда без лимита в 40 уровней).
- **`client/LibriClient`**: Клиентская точка входа (`ItemTooltipCallback.EVENT`):
  - Выводит `.desc`-описания для зачарований `libri` как на обычных/экипированных предметах (`stack.getEnchantments()`), так и на зачарованных книгах (`DataComponents.STORED_ENCHANTMENTS`).
  - Локализованный предпросмотр урона для `Sharpness` (`tooltip.libri.sharpness_damage`) при уровне > 5.
- **`client/ModMenuIntegration`**, **`config/ModConfigScreen`**: Экран Cloth Config через ModMenu (редактирует `removeAnvilCap`, `enchantmentMaxLevelMode`, `enchantmentCustomMaxLevel` в диапазоне 1–255).
- **`config/ModConfig`**: GSON-конфигурация (`config/libri.json`): `removeAnvilCap` (`true`), `enchantmentMaxLevelMode` (`CUSTOM`), `enchantmentCustomMaxLevel` (`10`, clamp `1..255`), `resonanceMaxBlocks` (`16`), `siftingJunkBlocks`, настройки `Trophy`.
- **`config/FurnaceEnchantmentData`**: Duck-интерфейс для `AbstractFurnaceBlockEntity` (`smeltingSpeed`, `fuelEfficiency`, `baseCookTimeTotal`).
- **`enchantment/ModEnchantments`**, **`ModEnchantmentHelper`**, **`ParalysisEffect`**: 14 зачарований + статус-эффект паралича.
- **`event/ModEventHandlers`**:
  - `Swift Strike` (тиковый модификатор скорости атаки с кэшем и очисткой на disconnect/respawn);
  - `Phoenix` (пассивный ремонт всех слотов инвентаря);
  - `Soulbound` (`COPY_FROM` при возрождении);
  - `Resonance` (цепочечная добыча руд через BFS с проверкой тега `ores`, сбором в `TelepathyHelper.RESONANCE_DROPS`).
- **`event/ModLootTables`**: Лут-таблицы сундуков подземелий и Древнего города (26.3 Loot API).
- **`ModCreativeTab`**: Вкладка `libri:main` с зачарованными книгами и `ADVANCED_ANVIL_ITEM`.

---

## Зачарования (14)

| Чара | Макс. уровень | Реализация | Допустимые предметы / слоты | Способ получения |
| :--- | :---: | :--- | :--- | :--- |
| **Eternity** | 1 | `ItemStackMixin` (`hurtAndBreak`) | `#minecraft:enchantable/durability` | Сундуки (15%), вкладка |
| **Phoenix** | I+ | `ModEventHandlers` (пассивный тик ремонта) | `#minecraft:enchantable/durability` | Сундуки (15%), вкладка |
| **Telekinesis** | 1 | `BlockDropMixin` + `EntityDropMixin` + `TelepathyHelper` | `#libri:enchantable/telekinesis` | Сундуки (15%), вкладка |
| **Smelting Touch** | 1 | `BlockDropMixin` (переплавка с поддержкой multi-stack) | `#libri:enchantable/smelting_touch` | Вкладка, лут, торговля |
| **Smelting Speed** | I+ | `FurnaceBlockEntityMixin` | `#libri:enchantable/furnace` | Случайный лут, торговля |
| **Fuel Efficiency** | I+ | `FurnaceBlockEntityMixin` | `#libri:enchantable/furnace` | Случайный лут, торговля |
| **Sacrifice** | I+ | `SacrificeMixin` + `EntityDropMixin` | Оружие (`mainhand`) | Сундуки (15%), вкладка |
| **Swift Strike** | I+ | `ModEventHandlers` | Оружие (`mainhand`) | Сундуки (15%), вкладка |
| **Hush** | 1 | `HushMixin`: глушит `GameEvent.STEP`, `HIT_GROUND`, `BLOCK_DESTROY` и звук/частицы ломания (`levelEvent` 2001) | Поножи (`HUSH`) или `mainhand` при ломании | Вкладка, лут, торговля (`treasure`) |
| **Soulbound** | 1 | `PlayerDropMixin` + `COPY_FROM` | Любые с прочностью/предметы | Древний город (2.5%), вкладка |
| **Sifting** | 1 | `BlockDropMixin` (`siftingJunkBlocks`) | `#libri:enchantable/sifting` | Вкладка, лут, торговля |
| **Resonance** | 1 | `ModEventHandlers` + `TelepathyHelper` | `#libri:enchantable/resonance` | Вкладка, лут, торговля |
| **Trophy** | 1 | `LivingEntityDropMixin` (головы и спавн-яйца) | `#libri:enchantable/trophy` | Вкладка, лут, торговля |
| **Paralysis** | 1 | `LibriMod` + `ParalysisEffect` | Луки и арбалеты | Сундуки (15%), вкладка |

---

## Миксины (22 общих + 1 клиентский)

- **Уровни и баланс:** `EnchantmentLevelMixin`, `EnchantmentMixin` (`areCompatible`), `EnchantmentIsAcceptableMixin` (`canEnchant`/`isSupportedItem`), `EnchantmentHelperMixin` (`canStoreEnchantments`), `SharpnessMixin` (`modifyDamage`), `LootingMixin`.
- **Наковальня:** `AnvilScreenHandlerMixin` (`createResult`), `AnvilScreenMixin` (Client, `extractLabels`), `ForgingScreenHandlerAccessor` (`access`, зарегистрирован в `libri.mixins.json`). Оба миксина снимают лимит 40 для `AdvancedAnvilBlock` всегда и для обычной наковальни при `ModConfig.get().removeAnvilCap`.
- **Точило:** `GrindstoneSlotMixin` (`GrindstoneMenu$2`, `$3`), `GrindstoneDisenchantMixin`, `GrindstoneTakeMixin` (`GrindstoneMenu$4`), `GrindstoneScreenHandlerAccessor`.
- **Печи:** `FurnaceBlockEntityMixin` (`ValueInput`/`ValueOutput`/`serverTick`), `FurnaceBlockEntityAccessor`, `BlockItemMixin`.
- **Дроп и сущности:** `BlockDropMixin`, `EntityDropMixin`, `LivingEntityDropMixin`, `SacrificeMixin`, `PlayerDropMixin`, `ItemStackMixin`, `HushMixin`.

---

## Исправления в `1.7.0`

1. Добавлен пропущенный `ForgingScreenHandlerAccessor` в `libri.mixins.json`.
2. Флаг конфига `ModConfig.removeAnvilCap` подключён в `AnvilScreenHandlerMixin` и `AnvilScreenMixin` для обычных наковален (`AdvancedAnvilBlock` работает без лимита всегда).
3. `HushMixin` теперь глушит не только шаги/падение, но и разрушение блоков (`GameEvent.BLOCK_DESTROY` и `levelEvent` 2001) при наличии чар на поножах или в основной руке.
4. Исправлена область видимости тултипов книг в `LibriClient` (`STORED_ENCHANTMENTS`) и локализован бонусный урон `Sharpness` (`tooltip.libri.sharpness_damage`).
5. Добавлены `assets/libri/icon.png`, `data/libri/loot_table/blocks/advanced_anvil.json`, ключ локализации `effect.libri.paralysis`, корректная проверка тегов руд в `Resonance` и удалены неиспользуемые `Scratch.java` / текстуры.
