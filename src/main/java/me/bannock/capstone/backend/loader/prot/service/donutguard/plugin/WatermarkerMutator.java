package me.bannock.capstone.backend.loader.prot.service.donutguard.plugin;

import com.google.inject.Inject;
import me.bannock.donutguard.obf.asm.entry.impl.ClassEntry;
import me.bannock.donutguard.obf.config.Configuration;
import me.bannock.donutguard.obf.mutator.Mutator;
import me.bannock.donutguard.utils.AsmUtils;
import org.objectweb.asm.tree.LdcInsnNode;

public class WatermarkerMutator extends Mutator {

    @Inject
    public WatermarkerMutator(Configuration config) {
        super("Watermarker", true);
        this.config = config;
    }

    private final static String API_KEY_PLACEHOLDER = "BNOK_%%API_KEY%%";
    private final static String UID_PLACEHOLDER = "BNOK_%%UID%%";
    private final static String AUTH_IP_PLACEHOLDER = "BNOK_%%AUTH_IP%%";

    private final Configuration config;

    @Override
    public void firstPassClassTransform(ClassEntry entry) {
        AsmUtils.loopOverAllInsn(entry, (methodNode, abstractInsn) -> {
            if (!(abstractInsn instanceof LdcInsnNode))
                return;
            LdcInsnNode ldc = (LdcInsnNode) abstractInsn;
            if (!(ldc.cst instanceof String))
                return;
            if (ldc.cst.equals(API_KEY_PLACEHOLDER))
                ldc.cst = WatermarkerConfigGroup.API_KEY.getString(config);
            else if (ldc.cst.equals(UID_PLACEHOLDER))
                ldc.cst = WatermarkerConfigGroup.UID.getString(config);
            else if (ldc.cst.equals(AUTH_IP_PLACEHOLDER))
                ldc.cst = WatermarkerConfigGroup.AUTH_SERVER_IP.getString(config);
        });
    }
}
