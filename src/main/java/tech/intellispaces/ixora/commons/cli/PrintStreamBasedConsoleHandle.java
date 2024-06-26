package tech.intellispaces.ixora.commons.cli;

import tech.intellispaces.framework.core.annotation.Mover;
import tech.intellispaces.framework.core.annotation.ObjectHandle;
import tech.intellispaces.ixora.cli.ConsoleMovableHandle;

import java.io.PrintStream;

@ObjectHandle
public abstract class PrintStreamBasedConsoleHandle implements ConsoleMovableHandle {
  private final PrintStream ps;

  public PrintStreamBasedConsoleHandle(PrintStream ps) {
    this.ps = ps;
  }

  @Mover
  @Override
  public ConsoleMovableHandle print(String message) {
    ps.print(message);
    return this;
  }

  @Mover
  @Override
  public ConsoleMovableHandle println(String message) {
    ps.println(message);
    return this;
  }
}
