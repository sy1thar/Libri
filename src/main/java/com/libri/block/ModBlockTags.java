package com.libri.block;

import com.libri.LibriMod;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModBlockTags {
    public static final TagKey<Block> HUSH_SUPPRESSED = TagKey.of(RegistryKeys.BLOCK, Identifier.of(LibriMod.MOD_ID, "hush_suppressed"));
}
