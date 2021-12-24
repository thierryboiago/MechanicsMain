package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.commands.testcommands.TestCommand;

public class WeaponMechanicsMainCommand extends MainCommand {

    public WeaponMechanicsMainCommand() {
        super("weaponmechanics", "weaponmechanics.admin");
        setDescription("WeaponMechanics main command");
        setAliases(StringUtil.getList("wea", "weapon", "wm"));

        commands.register(new TestCommand());
        commands.register(new GiveCommand());
        commands.register(new InfoCommand());
        commands.register(new ReloadCommand());
        commands.register(new WikiCommand());
    }
}
