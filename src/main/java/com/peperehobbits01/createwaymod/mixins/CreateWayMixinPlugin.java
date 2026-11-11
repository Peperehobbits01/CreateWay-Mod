package com.peperehobbits01.createwaymod.mixins;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

public class CreateWayMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MORE_BABIES_MIXIN =
        "com.peperehobbits01.createwaymod.mixins.MoreBabiesOceanMonumentPieceMixin";
    private static final String END_REMASTERED_MIXIN =
        "com.peperehobbits01.createwaymod.mixins.EndRemasteredEyeMixin";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals(MORE_BABIES_MIXIN)) {
            return FMLLoader.getLoadingModList().getModFileById("more_babies") != null;
        }
        if (mixinClassName.equals(END_REMASTERED_MIXIN)) {
            return FMLLoader.getLoadingModList().getModFileById("endrem") != null;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!mixinClassName.equals(MORE_BABIES_MIXIN)) {
            return;
        }

        String configClass = "com/github/mammut53/more_babies/config/MoreBabiesConfig";
        String levelClass = "net/minecraft/world/level/Level";
        String randomSource = "Lnet/minecraft/util/RandomSource;";
        String helperClass = "com/peperehobbits01/createwaymod/compat/MoreBabiesWorldgenRandom";
        int replacements = 0;

        for (MethodNode method : targetClass.methods) {
            boolean usesMoreBabiesConfig = false;
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call
                    && call.owner.equals(configClass)) {
                    usesMoreBabiesConfig = true;
                    break;
                }
            }
            if (!usesMoreBabiesConfig) {
                continue;
            }

            for (var instruction : method.instructions) {
                if (instruction instanceof FieldInsnNode field
                    && field.getOpcode() == Opcodes.GETFIELD
                    && field.owner.equals(levelClass)
                    && field.name.equals("random")
                    && field.desc.equals(randomSource)) {
                    method.instructions.set(
                        field,
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            helperClass,
                            "getWorldgenRandom",
                            "(L" + levelClass + ";)" + randomSource,
                            false
                        )
                    );
                    replacements++;
                }
            }
        }

        if (replacements == 0) {
            LOGGER.warn("More Babies' Ocean Monument random access was not found; C2ME compatibility was not applied");
        } else {
            LOGGER.info("Using world-generation random for More Babies' Ocean Monument baby roll");
        }
    }
}
