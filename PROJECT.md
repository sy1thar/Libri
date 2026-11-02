# Libri — паспорт проекта (для контекста ИИ)

Fabric-мод, оверхоул зачарований и механик снаряжения.
**Версия мода:** `1.6.1` | **MC:** `1.21.1` | **Java:** `21` | **Fabric Loader:** `≥0.16.0` | **Fabric API:** `0.110.0+1.21.1` | **Cloth Config:** `≥15.0.0` | **ModMenu:** `11.0.3` (опционально).
**MOD_ID:** `libri`. Серверный мод с клиентской частью (тултипы `LibriClient` и миксин `AnvilScreenMixin`).

---

## Структура (`com.libri`)

- **`LibriMod`**: Точка входа `main`. Регистрация статус-эффекта `PARALYSIS_EFFECT` (`libri:paralysis`), блока и предмета `ADVANCED_ANVIL` (`libri:advanced_anvil`), инициализация конфига, событий, таблиц лута и креативной вкладки.
  - Обработчик `ServerLivingEntityEvents.ALLOW_DAMAGE`: парализованная сущность не может наносить урон; стрела, выпущенная игроком с чарами `Paralysis` на луке/арбалете (проверяются `mainHand`/`offHand`), накладывает паралич на 600 тиков (30 сек). *Edge-case:* проверка чар стрелка происходит по его руке в момент попадания, а не в момент выстрела.
- **`block/AdvancedAnvilBlock`**: Блок улучшенной наковальни (наследник `AnvilBlock`), открывающий `AnvilScreenHandler` с заголовком `container.libri.advanced_anvil`. Используется для снятия ванильного лимита стоимости ремонта/ковки.
- **`client/LibriClient`**: Точка входа `client`. Обработчик `ItemTooltipCallback.EVENT`:
  - Добавляет описание `.desc` для всех зачарований с пространством имён `libri` на экипировке (на книгах описание не отображается, так как они используют компонент `STORED_ENCHANTMENTS`).
  - Предпросмотр урона для `Sharpness`: при уровне Sharpness > 5 динамически рассчитывает урон с учётом прогрессивного бонуса мода и выводит строку `%.1f Attack Damage (enchanted)`.
- **`client/ModMenuIntegration`**: Интеграция с ModMenu через Cloth Config (`ModConfigScreen`).
- **`config/ModConfig`**: Конфигурация мода через GSON (`config/libri.json`):
  - `removeAnvilCap` (`boolean`, по умолч. `true`) — *Внимание:* в текущей реализации миксинов наковальни этот флаг не задействован, снятие лимита привязано к блоку `AdvancedAnvilBlock`.
  - `enchantmentMaxLevelMode` (`VANILLA` | `CUSTOM`, по умолч. `CUSTOM`).
  - `enchantmentCustomMaxLevel` (`int`, по умолч. `10`, диапазон 1–10000).
  - `resonanceMaxBlocks` (`int`, по умолч. `16`).
  - `siftingJunkBlocks` (`List<String>`: булыжник, глубосланец, туф, гранит, диорит, андезит, гравий).
  - `trophyMaxLevel` (`int`, по умолч. `1`).
  - `trophyHeadBaseChance` (`float`, по умолч. `0.08f`).
  - `trophyHeadLevelMultiplier` (`float`, по умолч. `0.0f`).
  - `trophyEggMinLevel` (`int`, по умолч. `1`).
  - `trophyEggChance` (`float`, по умолч. `0.02f`).
- **`config/ModConfigScreen`**: GUI настроек (Cloth Config). В текущей версии отображает параметры лимита наковальни и максимального уровня зачарований.
- **`config/FurnaceEnchantmentData`**: Duck-интерфейс для `AbstractFurnaceBlockEntity` (геттеры/сеттеры для `smeltingSpeed`, `fuelEfficiency`, `baseCookTimeTotal`).
- **`enchantment/ModEnchantments`**: 14 ключей `RegistryKey<Enchantment>` (`ETERNITY`, `TELEKINESIS`, `PHOENIX`, `SACRIFICE`, `SWIFT_STRIKE`, `SMELTING_SPEED`, `FUEL_EFFICIENCY`, `SMELTING_TOUCH`, `HUSH`, `SOULBOUND`, `SIFTING`, `RESONANCE`, `TROPHY`, `PARALYSIS`).
- **`enchantment/ModEnchantmentHelper`**: Утилиты извлечения уровня чар (`getLevel` по `ItemStack` и `ItemEnchantmentsComponent`).
- **`enchantment/ParalysisEffect`**: Вредный эффект (`HARMFUL`, цвет `0x800080`), снижает скорость передвижения на 100% (`GENERIC_MOVEMENT_SPEED` -1.0 `ADD_MULTIPLIED_TOTAL`).
- **`event/ModEventHandlers`**:
  - `ServerTickEvents.END_SERVER_TICK`:
    - `Swift Strike`: добавляет временный модификатор `GENERIC_ATTACK_SPEED` (`+0.5 * level`, `ADD_MULTIPLIED_BASE`). Состояние кэшируется в `SWIFT_LEVEL_CACHE`.
    - `Phoenix`: восстанавливает 1 ед. прочности каждые `max(1, 100 / level)` тиков. Проверяет все 41 слот инвентаря игрока (основной 0..35, броня 36..39, оффхенд 40).
  - `ServerPlayConnectionEvents.DISCONNECT`: очистка игрока из `SWIFT_LEVEL_CACHE`.
  - `ServerPlayerEvents.AFTER_RESPAWN`: очистка игрока из `SWIFT_LEVEL_CACHE` (баг сброса модификатора после смерти исправлен).
  - `ServerPlayerEvents.COPY_FROM`: перенос предметов с зачарованием `Soulbound` при возрождении игрока.
  - `PlayerBlockBreakEvents.BEFORE`: цепная добыча руд `Resonance`. Поиск в ширину (BFS) однотипных руд до лимита `resonanceMaxBlocks`, разрушение через `tryBreakBlock`, сбор и объединение выпавших предметов через `TelepathyHelper.RESONANCE_DROPS` в один стак на месте исходного блока.
- **`event/ModLootTables`**:
  - Сундуки подземелий (10 структур, 15% шанс): случайная книга из пула (`Eternity`, `Telekinesis`, `Phoenix`, `Sacrifice`, `Swift Strike`, `Paralysis`). При сбое выдаётся обычная книга (`Items.BOOK`).
  - Сундуки Древнего города (`chests/ancient_city`, 2.5% шанс): зачарованная книга `Soulbound`.
- **`helper/TelepathyHelper`**: ThreadLocal-переменные: `TELEKINESIS_ATTACKER`, `BLOCK_MINER`, `RESONANCE_ORIGIN`, `RESONANCE_DROPS`.
- **`ModCreativeTab`**: Вкладка `libri:main`, иконка — книга с `CustomModelData=1`. Добавляет 12 зачарованных книг (все, кроме печных `smelting_speed` и `fuel_efficiency`) и блок `ADVANCED_ANVIL_ITEM`.

---

## Зачарования (14)

| Чара | Макс. уровень | Реализация | Допустимые предметы / слоты | Способ получения |
| :--- | :---: | :--- | :--- | :--- |
| **Eternity** | 1 | `ItemStackMixin`: отмена метода `damage()` | `#minecraft:enchantable/durability` (Любое с прочностью) | Сундуки сокровищ (15%), вкладка |
| **Phoenix** | I+ | `ModEventHandlers`: пассивный тик ремонта (41 слот) | `#minecraft:enchantable/durability` | Сундуки сокровищ (15%), вкладка |
| **Telekinesis** | 1 | `BlockDropMixin` + `EntityDropMixin` + `TelepathyHelper` | `#libri:enchantable/telekinesis` (Оружие и инструменты) | Сундуки сокровищ (15%), вкладка |
| **Smelting Touch** | 1 | `BlockDropMixin.smeltDrops`: автопереплавка руд без XP | `#libri:enchantable/smelting_touch` (Кирки) | Вкладка, случайный лут, торговля |
| **Smelting Speed** | I+ | `FurnaceBlockEntityMixin`: ускорение времени плавки `÷(1 + 2L²)` | `#libri:enchantable/furnace` (Печи, плавильни, коптильни) | Случайный лут, торговля |
| **Fuel Efficiency** | I+ | `FurnaceBlockEntityMixin`: шанс `L / (L + 10)` на +1 тик горения | `#libri:enchantable/furnace` | Случайный лут, торговля |
| **Sacrifice** | I+ | `SacrificeMixin` + `EntityDropMixin`: лут мобов → опыт `×(1 + 0.5·Looting)` | Оружие (`mainhand`). Защита от PvP-воровства | Сундуки сокровищ (15%), вкладка |
| **Swift Strike** | I+ | `ModEventHandlers`: модификатор скорости атаки `+0.5·L` | Оружие (`mainhand`) | Сундуки сокровищ (15%), вкладка |
| **Hush** | 1 | `HushMixin`: глушение вибраций `GameEvent.STEP` (шаги) | `#libri:enchantable/hush` (Поножи). *Звуки блоков не глушит* | Вкладка, случайный лут, торговля (`treasure: true`) |
| **Soulbound** | 1 | `PlayerDropMixin` (сохранение) + `COPY_FROM` (восстановление) | `#minecraft:enchantable/vanishing` (Любые предметы) | Сундуки Ancient City (2.5%), вкладка |
| **Sifting** | 1 | `BlockDropMixin`: уничтожение мусорных блоков из конфига | `#libri:enchantable/sifting` (Кирки, лопаты) | Вкладка, случайный лут, торговля |
| **Resonance** | 1 | `ModEventHandlers` + `TelepathyHelper`: цепная добыча руд | `#libri:enchantable/resonance` (Кирки) | Вкладка, случайный лут, торговля |
| **Trophy** | 1 | `LivingEntityDropMixin`: головы (8%) и спавн-яйца (2%) | `#libri:enchantable/trophy` (Мечи, топоры). Защита от PvP | Вкладка, случайный лут, торговля |
| **Paralysis** | 1 | `LibriMod` + `ParalysisEffect`: стрела обездвиживает на 30 сек | Луки и арбалеты (`mainhand`, `offhand`) | Сундуки сокровищ (15%), вкладка |

---

## Миксины (21 общий + 1 клиентский)

### Уровни, баланс и совместимость
- **`EnchantmentLevelMixin`**: В режиме `CUSTOM` для всех зачарований с ванильным макс. уровнем ≥ 2 возвращает `enchantmentCustomMaxLevel`. Масштабирует `minPower` (1..30) и устанавливает `maxPower` ≥ 50.
- **`EnchantmentMixin`**: Переопределяет `canBeCombined`. Если наборы слотов экипировки зачарований пересекаются (`libri$slotsOverlap`), зачарования признаются совместимыми (позволяет совмещать взаимоисключающие чары: Защиту со Снарядостойкостью, Шёлк с Удачей и т.д.).
- **`EnchantmentIsAcceptableMixin`**: Делает любое зачарование с префиксом `enchantment.libri.` применимым к **любому** предмету, полностью перебивая JSON-фильтры `supported_items`.
- **`EnchantmentHelperMixin`**: Расширяет метод `canHaveEnchantments` для предметов из тега `#libri:enchantable/furnace`.
- **`SharpnessMixin`**: Прогрессивный урон Остроты при уровне > 5 по формуле `base + bonus * (1 + (level - 5) * 0.4)`.
- **`LootingMixin`**: Увеличивает множитель Добычи при уровне > 3 втрое (`level * 3`).

### Наковальня
- **`AnvilScreenHandlerMixin`**:
  - Проверяет блок в контексте: если это `com.libri.LibriMod.ADVANCED_ANVIL`, заменяет лимит уровней с 40 на 99999.
  - Метод `preventWastingEnchantments`: обнуляет результат на выходе наковальни, если не изменились имя, прочность и зачарования.
- **`AnvilScreenMixin` (Client)**: В методе отрисовки `drawForeground` заменяет проверку лимита 40 на 99999, если заголовок экрана совпадает с `container.libri.advanced_anvil`.
- ⚠️ **`ForgingScreenHandlerAccessor`**: Accessor-интерфейс к полю `context`. **ОШИБКА РЕГИСТРАЦИИ:** отсутствует в `libri.mixins.json`!

### Точило (Снятие зачарований)
- **`GrindstoneSlotMixin`**: Разрешает класть обычную книгу (`Items.BOOK`) в слоты точила ($2 и $3).
- **`GrindstoneDisenchantMixin`**: При комбинации зачарованного предмета (не зачарованной книги) и обычной книги формирует в результате зачарованную книгу с первым зачарованием предмета (с `CustomModelData=1` для чар Libri).
- **`GrindstoneTakeMixin`**: При забирании результата снимает извлечённое зачарование с исходного предмета, сбрасывает `REPAIR_COST` на 0, уменьшает количество обычных книг на 1 и начисляет игроку опыт `ceil(level / 2)`.
- **`GrindstoneScreenHandlerAccessor`**: Accessor к полям `input` и `context`.

### Печи
- **`FurnaceBlockEntityMixin`**: Сохраняет и загружает уровни `smelting_speed`, `fuel_efficiency` и `base_cook_time` в NBT печи. В методе `tick` динамически масштабирует `cookTimeTotal` и добавляет тики горения топлива.
- **`FurnaceBlockEntityAccessor`**: Accessor к полям `cookTime`, `fuelTime`, `cookTimeTotal`.
- **`BlockItemMixin`**: При установке печи переносит зачарования `Smelting Speed` и `Fuel Efficiency` из `ItemStack` в `FurnaceBlockEntity`.

### Дроп и сущности
- **`BlockDropMixin`**: Перехватывает оба метода `dropStacks` (6-аргументный и 3-аргументный). Обрабатывает Телекинез (выдача в инвентарь), Автоплавку (`smeltDrops`), Просеивание (удаление мусора по списку `siftingJunkBlocks`), Резонанс (перенаправление и объединение лута) и сохранение зачарований печи при её разрушении.
- **`EntityDropMixin`**: Перехватывает `Entity.dropStack`. При активном `TELEKINESIS_ATTACKER` отправляет дроп в инвентарь или конвертирует в опыт при наличии `Sacrifice`.
- **`LivingEntityDropMixin`**: Устанавливает `TELEKINESIS_ATTACKER` при гибели сущностей (игнорирует игроков). Реализует механику `Trophy` (выпадение голов и яиц призыва).
- **`SacrificeMixin`**: Перехватывает `dropLoot`, рассчитывает лут сущности через контекст лут-таблиц, конвертирует предметы в сферы опыта и отменяет стандартный дроп (игнорирует игроков).
- **`PlayerDropMixin`**: Перехватывает `PlayerInventory.dropAll`: временно извлекает предметы с `Soulbound`, исключая их выпадение на землю, и возвращает обратно в те же слоты.
- **`ItemStackMixin`**: Блокирует метод `damage()`, если на предмете есть `Eternity`.
- **`HushMixin`**: Отменяет событие `GameEvent.STEP`, если на игроке надеты поножи с `Hush`.

---

## Ресурсы и конфигурационные данные

- **Зачарования JSON (`data/libri/enchantment/`):** 14 файлов определений зачарований. `hush` помечен `"treasure": true`.
- **Теги предметов (`data/libri/tags/item/enchantable/`):** `furnace`, `hush`, `resonance`, `sifting`, `smelting_touch`, `telekinesis`, `trophy`.
- **Теги ванильных чар (`data/minecraft/tags/enchantment/`):**
  - `treasure.json`: `libri:paralysis`.
  - `non_treasure.json`: 9 чар.
  - `on_random_loot.json`: 13 чар.
  - `tradeable.json`: 13 чар.
- **Блоки и рецепты:**
  - `data/libri/recipe/advanced_anvil.json`: крафт улучшенной наковальни (3 железных блока, 2 золотых слитка, наковальня, 3 обсидиана).
  - `data/minecraft/tags/block/anvil.json`: `libri:advanced_anvil`.
  - Модель и текстура блока `advanced_anvil` присутствуют в ресурсах.
- **Локализация (`assets/libri/lang/`):**
  - `en_us.json`, `ru_ru.json`, `uk_ua.json` содержат переводы названий и описаний всех 14 зачарований, блока наковальни и параметров конфига.
  - *Недостающий ключ:* отсутствует `effect.libri.paralysis` (для отображения эффекта в инвентаре).

---

## Актуальный баг-трекер и приоритетные задачи

### P0 (Критические баги):
1. **Отсутствие `ForgingScreenHandlerAccessor` в `libri.mixins.json`:** Вызов `((ForgingScreenHandlerAccessor) this).getContext()` в `AnvilScreenHandlerMixin` вызовет `ClassCastException` в чистом клиенте/сервере. Требуется добавить класс в список миксинов.

### P1 (Функциональные расхождения и ассеты):
2. **Неработающий параметр `removeAnvilCap`:** В `ModConfig` есть настройка `removeAnvilCap`, но логика миксинов наковальни целиком завязана на наличие блока `AdvancedAnvilBlock`. Необходимо либо учесть этот флаг для обычной наковальни, либо актуализировать описание настройки.
3. **Отсутствие `icon.png`:** В `src/main/resources/assets/libri/` лежит только `icon.psd`, из-за чего в ModMenu и списке модов иконка отсутствует.
4. **Неполная реализация `Hush`:** Описание в README и lang обещает «бесшумное ломание блоков и скрытие шагов», но миксин `HushMixin` отменяет только `GameEvent.STEP`. `GameEvent.BLOCK_DESTROY` не обрабатывается.
5. **Локализация статус-эффекта:** Добавить ключ `effect.libri.paralysis` во все три языковых файла (`en_us`, `ru_ru`, `uk_ua`).

### P2 (QoL, рефакторинг и чистота кода):
6. **Тексты описания мода:** `fabric.mod.json` и лог инициализации в `LibriMod` по-прежнему упоминают «9 enchantments» вместо 14.
7. **Удаление остаточного кода:** Удалить тестовый класс `com.libri.scratch.Scratch` и неиспользуемые текстуры `alchemy_table_*`, `pedestals_*`.
8. **Калькулятор урона `Sharpness`:** Строка `%.1f Attack Damage (enchanted)` в `LibriClient` захардкожена на английском языке без использования переводов.
9. **`EnchantmentIsAcceptableMixin`:** Принудительное разрешение чар Libri для любых предметов делает неактуальными теги `supported_items` в JSON зачарований.

---

## Стиль и архитектурные конвенции проекта

- Duck-интерфейсы (`FurnaceEnchantmentData`) и методы с префиксом `libri$` для внедрения состояний в ванильные BlockEntity.
- Изолированные окна через `ThreadLocal` (`TelepathyHelper`) для проброса контекста добычи и атак между независимыми игровыми событиями.
- Приоритет инъекций в `@At("HEAD")` с флагом `cancellable = true` для перехвата стандартного дропа и урона.
- Конфигурация через POJO-класс Gson с авто-сохранением при закрытии меню Cloth Config.
