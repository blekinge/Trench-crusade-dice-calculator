package dk.askov.dicecalc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComputeRunner {
    
    enum Backend {OPENCL, PTX, METAL}
    
    record Config(
        boolean useGpu, Backend backend, String gpuMemory,
        String heapMin, String heapMax,
        boolean debug, boolean profiler, String profilerDumpDir,
        boolean printBytecodes, boolean threads, boolean printKernel,
        boolean fullDump, boolean verboseInit,
        boolean showCommand, boolean executeAfterShow,
        String openclFlags, int maxWaitEvents, boolean verbose
    ) {
    }
    
    static String modulePath(String tornadoSdk) {
        var sep = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";
        return "." + sep + tornadoSdk + "/share/java/tornado";
    }
    
    static void main() {
        var command = buildCommand(new Config(false, Backend.OPENCL, null, null, null, false, false, null, false, false, false, false , false, false, false, null, 0, true), "/home/aabl/.sdkman/candidates/java/25.0.3-graal", "/home/aabl/.sdkman/candidates/tornadovm/4.0.1-jdk25-opencl");
        System.out.println(command.stream().collect(Collectors.joining(" ")));
    }
    
    static List<String> buildCommand(Config cfg, String javaHome, String tornadoSdk) {
        var cmd = new ArrayList<String>();
        
        cmd.addAll(List.of(
            javaHome + "/bin/java",
            "-server",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+EnableJVMCI",
            //"-Xms" + cfg.heapMin(),
            //"-Xmx" + cfg.heapMax(),
            "--enable-preview",
            "-Djava.library.path=" + tornadoSdk + "/lib",
            "-Djdk.module.showModuleResolution=false",
            "--module-path", modulePath(tornadoSdk)
                          ));
        
        // TornadoVM configuration
        cmd.addAll(List.of(
            "-Dtornado.load.api.implementation=uk.ac.manchester.tornado.runtime.tasks.TornadoTaskGraph",
            "-Dtornado.load.runtime.implementation=uk.ac.manchester.tornado.runtime.TornadoCoreRuntime",
            "-Dtornado.load.tornado.implementation=uk.ac.manchester.tornado.runtime.common.Tornado",
            "-Dtornado.load.annotation.implementation=uk.ac.manchester.tornado.annotation.ASMClassVisitor",
            "-Dtornado.load.annotation.parallel=uk.ac.manchester.tornado.api.annotations.Parallel",
            "-Dtornado.tvm.maxbytecodesize=65536"
                          ));
        
        if (cfg.useGpu()) {
            cmd.add("-Duse.tornadovm=true");
        }
        if (cfg.verboseInit()) {
            cmd.add("-Dllama.EnableTimingForTornadoVMInit=true");
        }
        
        // Debug flags
        cmd.add("-Dtornado.debug=" + cfg.debug());
        cmd.add("-Dtornado.threadInfo=" + cfg.threads());
        cmd.add("-Dtornado.fullDebug=" + cfg.fullDump());
        cmd.add("-Dtornado.printKernel=" + cfg.printKernel());
        cmd.add("-Dtornado.print.bytecodes=" + cfg.printBytecodes());
        
        // Runtime configuration
        cmd.addAll(List.of(
            //"-Dtornado.device.memory=" + cfg.gpuMemory(),
            "-Dtornado.profiler=" + cfg.profiler(),
            "-Dtornado.log.profiler=false",
            "-Dtornado.profiler.dump.dir=" + cfg.profilerDumpDir(),
            "-Dtornado.enable.fastMathOptimizations=true",
            "-Dtornado.enable.mathOptimizations=false",
            "-Dtornado.enable.nativeFunctions=true",
            "-Dtornado.loop.interchange=true",
            "-Dtornado.eventpool.maxwaitevents=" + cfg.maxWaitEvents()
                          ));
        
        if (cfg.backend() == Backend.OPENCL) {
            cmd.add("-Dtornado.opencl.compiler.flags=" + cfg.openclFlags());
        }
        
        // Module configuration
        cmd.addAll(List.of(
            "--upgrade-module-path", tornadoSdk + "/share/java/graalJars",
            "@" + tornadoSdk + "/etc/exportLists/common-exports"
                          ));
        
        switch (cfg.backend()) {
            case OPENCL -> {
                cmd.add("@" + tornadoSdk + "/etc/exportLists/opencl-exports");
                cmd.addAll(List.of("--add-modules",
                                   "ALL-SYSTEM,jdk.incubator.vector,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.opencl"));
            }
            case PTX -> {
                cmd.add("@" + tornadoSdk + "/etc/exportLists/ptx-exports");
                cmd.addAll(List.of("--add-modules",
                                   "ALL-SYSTEM,jdk.incubator.vector,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.ptx"));
            }
            case METAL -> {
                cmd.add("@" + tornadoSdk + "/etc/exportLists/metal-exports");
                cmd.addAll(List.of("--add-modules",
                                   "ALL-SYSTEM,jdk.incubator.vector,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.metal"));
            }
        }
        
        
        return cmd;
    }
    
}
