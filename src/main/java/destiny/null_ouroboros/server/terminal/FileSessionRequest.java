package destiny.null_ouroboros.server.terminal;

import destiny.null_ouroboros.server.terminal.filesystem.TerminusTextFile;

import javax.annotation.Nullable;

public record FileSessionRequest(TerminusTextFile file, FileSessionMode mode) {}
