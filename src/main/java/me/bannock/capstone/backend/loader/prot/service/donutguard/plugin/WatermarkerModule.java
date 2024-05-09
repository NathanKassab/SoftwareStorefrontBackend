package me.bannock.capstone.backend.loader.prot.service.donutguard.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.bannock.donutguard.obf.config.ConfigurationGroup;
import me.bannock.donutguard.obf.mutator.Mutator;

public class WatermarkerModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<Mutator> mutatorMultibinder = Multibinder.newSetBinder(binder(), Mutator.class);
        mutatorMultibinder.addBinding().to(WatermarkerMutator.class);

        Multibinder<ConfigurationGroup> configurationGroupMultibinder =
                Multibinder.newSetBinder(binder(), ConfigurationGroup.class);
        configurationGroupMultibinder.addBinding().to(WatermarkerConfigGroup.class);
    }

}
