package destiny.null_ouroboros.server.terminal.command;

import destiny.null_ouroboros.server.terminal.TerminalCommand;
import destiny.null_ouroboros.server.terminal.filesystem.TerminusFileSystem;
import net.minecraft.core.BlockPos;

public class CommandShutdown extends TerminalCommand {
    private final String args;

    public CommandShutdown(TerminusFileSystem fs, BlockPos pos, String args) {
        super(fs, pos);
        this.args = args.trim();
    }

    @Override
    public void execute() {
        if (!args.isEmpty()) {
            printlnTranslatable("message.null_ouroboros.terminus.shutdown.usage");
            setDone();
            return;
        }

        setDone();
    }

    @Override
    public boolean requestsShutdown() {
        return args.isEmpty();
    }
}
