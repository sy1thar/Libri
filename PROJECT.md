Libri — паспорт проекта (для контекста ИИ)

Fabric-мод, оверхоул зачарований. MC 1.21.1, Java 21, Fabric API, Cloth Config ≥15,ModMenu (опц., entrypoint есть). MOD_ID: libri. Серверный (1 клиентский миксин).
Структура (com.libri)

    LibriMod: init; paralysis StatusEffect + ALLOW_DAMAGE-логика (парализованный не бьёт;стрела с Paralysis → 600т паралича; проверка чар ПО РУКАМ в момент попадания — известный edge)
    client/LibriClient: тултипы .desc (только libri-чары, ENCHANTMENTS — на книгах не видно!)
        расчёт урона Sharpness; client/ModMenuIntegration → ModConfigScreen (Cloth, мгновенное применение)
    ModCreativeTab: 11 книг (НЕТ smelting_speed, fuel_efficiency, paralysis), CMD=1,кастом-модель через оверрайд minecraft:enchanted_book
    enchantment/: ModEnchantments — 9 RegistryKey (PARALYSIS нет — инлайн в LibriMod!);ModEnchantmentHelper.getLevel(key, stack|component); ParalysisEffect (speed ×0, HARMFUL)
    config/: ModConfig (gson, config/libri.json, автосоздания/логов/клампов нет:removeAnvilCap, maxLevelMode VANILLA|CUSTOM, customMaxLevel=10), FurnaceEnchantmentData(duck-интерфейс печей; лежит не в своём пакете)
    event/: ModEventHandlers (Swift Strike: +0.5 базы ATTACK_SPEED/ур, UUID-кэш — БАГ: не чиститсяпри смерти; Phoenix: 1пр/100L тиков, main+armor, offhand пропущен);ModLootTables (5 чар × 10 сундуков, 15%; при сбое — пустая зачар. книга)
    helper/TelepathyHelper: ThreadLocal TELEKINESIS_ATTACKER, BLOCK_MINER
    МЁРТВЫЙ КОД (подтверждено): block/entity (ModBlockEntities), blockstates/, models/block/,loot_table/blocks/, block.libri.*/container/message в lang — остатки «алхим. стол + 5 пьедесталов»

Зачарования (10)
Чара	Макс	Реализация	Добыча
Eternity	1	ItemStackMixin: cancel damage()	сундуки (treasure)
Phoenix	I+	ModEventHandlers, тик	сундуки
Telekinesis	1	BlockDrop+EntityDrop+ThreadLocal	сундуки, вкладка
Smelting Touch	1	BlockDropMixin.smeltDrops (XP плавки не даёт)	вкладка
Smelting Speed	I+	печь: ÷(1+2L²) квадр.	стол (печка в слоте)
Fuel Efficiency	I+	печь: +1тик с шансом L/(L+10)	стол
Sacrifice	I+	Sacrifice+EntityDrop+LivingEntityDrop, лут→XP ×(1+0.5·Loot)	сундуки
Swift Strike	I+	attr-модификатор ATTACK_SPEED	сундуки
Hush	1	HushMixin: STEP-глушение и глушение ломания блоков	вкладка
Soulbound	1	сохранение в инвентаре после смерти	вкладка
Sifting	1	удаление мусорных блоков при добыче	вкладка
Resonance	1	цепная добыча руд	вкладка
Trophy	1	повышенный шанс голов и спавн-яиц	вкладка
Paralysis	1	LibriMod + ParalysisEffect	НЕТ: не в луте/вкладке, без переводов
Миксины (20 + client)

Уровни: EnchantmentLevelMixin (глобально все чары макс≥2 → кастом, minPower 1→30, maxPower≥50),EnchantmentMixin (canBeCombined: эксклюзивные наборы СОВМЕСТИМЫ при пересечении слотов — НЕ в README!),EnchantmentIsAcceptableMixin (libri-чары применимы К ЛЮБОМУ предмету — перебивает JSON supported_items!),EnchantmentHelperMixin (canHaveEnchantments: предметы из libri:enchantable/furnace)SharpnessMixin (Sharpness>5: бонус ×(1+0.4(L−5))), LootingMixin (getEquipmentLevel Looting>3 ×3 —  возможно мёртв: в 1.21 лут идёт через WEAPON-параметр, которого в Sacrifice-контексте НЕТ)Точило: Disenchant (первое чаро → книга), Take (забор: остаток+repair_cost=0+XP ceil(L/2),  double-write, выбор чара недетерминирован), Slot ($2/$3 пускают книгу), ScreenHandlerAccessorНаковальня: AnvilScreenHandlerMixin + AnvilScreenMixin (40→99999, флаг removeAnvilCap)Печи: FurnaceBlockEntityMixin (tick-хук; NBT libri:*) + Accessor; BlockItemMixin (postPlacement перенос чар)Дроп: BlockDropMixin (6-арг + 3-арг; телекинез/smelt/перенос чар печки при ломании),  EntityDropMixin, LivingEntityDropMixin (окно ThreadLocal вокруг drop()), SacrificeMixin
Данные

    JSON чар: seen smelting_speed (furnace tag, ml10, w2) и eternity (durability, ml1, treasure);прочие 8 — аналогичной структуры (уточнять при задачах)
    tags/item/enchantable/furnace: furnace, blast_furnace, smoker (модовых печей нет)
    non_treasure.json — вероятно инертен в 1.21.1 (treasure = поле JSON)
    lang: 9/10 чар переведены (paralysis НЕТ: имя, .desc, effect.*), мёртвые block-ключи,бесполезный оверрайд item.minecraft.enchanted_book; ru_ru — проверить зеркальность
    fabric.mod.json: entrypoints ок (main/client/modmenu), icon → assets/libri/icon.png(в дереве виден только icon.psd — проверить!), authors/contact пусты, в описании Hush нет

Приоритетные проблемы

P0: 1) Paralysis: переводы + способ добычи + ключ в ModEnchantments; 2) Swift Strike теряется    после смерти (чистить кэш при respawn); 3) PvP: телекинез/жертва воруют инвентарь игроков (тест+гейт)P1: 4) IsAcceptableMixin vs JSON — решить: фича «универсальные чары» или ограничить;    5) пустая книга в луте при сбое; 6) удалить мёртвый блок-код; 7) icon.png; 8) Phoenix offhandP2: i18n (хардкод "Attack Damage", .desc на книгах), WEAPON в лут-контексте, клампы конфига/скейлов,    недетерминизм точила, getCookTime-хук вместо tick, README (Hush, Paralysis, совместимость    эксклюзивных чар, универсальность)
Стиль кода

Duck-интерфейсы + @Unique для BE-данных; ThreadLocal-окна; HEAD-cancel миксины; RegistryKey-константы; конфиг через gson-POJO. Писать в этом стиле.