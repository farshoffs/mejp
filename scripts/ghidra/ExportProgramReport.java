// Export a compact, machine-readable Ghidra report for offline review.
//@category MyJPJ

import java.io.File;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.symbol.Symbol;
import ghidra.program.model.symbol.SymbolIterator;

public class ExportProgramReport extends GhidraScript {
    @Override
    public void run() throws Exception {
        String[] args = getScriptArgs();
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: ExportProgramReport <output.json>");
        }

        JsonObject root = new JsonObject();
        root.addProperty("name", currentProgram.getName());
        root.addProperty("executableFormat", currentProgram.getExecutableFormat());
        root.addProperty("executablePath", currentProgram.getExecutablePath());
        root.addProperty("languageId", currentProgram.getLanguageID().toString());
        root.addProperty("compilerSpecId", currentProgram.getCompilerSpec().getCompilerSpecID().toString());
        root.addProperty("imageBase", currentProgram.getImageBase().toString());
        root.addProperty("minAddress", currentProgram.getMinAddress().toString());
        root.addProperty("maxAddress", currentProgram.getMaxAddress().toString());

        JsonArray blocks = new JsonArray();
        for (MemoryBlock block : currentProgram.getMemory().getBlocks()) {
            JsonObject item = new JsonObject();
            item.addProperty("name", block.getName());
            item.addProperty("start", block.getStart().toString());
            item.addProperty("end", block.getEnd().toString());
            item.addProperty("size", block.getSize());
            item.addProperty("read", block.isRead());
            item.addProperty("write", block.isWrite());
            item.addProperty("execute", block.isExecute());
            item.addProperty("initialized", block.isInitialized());
            blocks.add(item);
        }
        root.add("memoryBlocks", blocks);

        JsonArray functions = new JsonArray();
        FunctionIterator fit = currentProgram.getFunctionManager().getFunctions(true);
        while (fit.hasNext() && !monitor.isCancelled()) {
            Function function = fit.next();
            JsonObject item = new JsonObject();
            item.addProperty("name", function.getName());
            item.addProperty("entry", function.getEntryPoint().toString());
            item.addProperty("external", function.isExternal());
            item.addProperty("thunk", function.isThunk());
            item.addProperty("callingConvention", function.getCallingConventionName());
            functions.add(item);
        }
        root.add("functions", functions);

        JsonArray symbols = new JsonArray();
        SymbolIterator sit = currentProgram.getSymbolTable().getAllSymbols(true);
        while (sit.hasNext() && !monitor.isCancelled()) {
            Symbol symbol = sit.next();
            if (symbol.isExternal() || symbol.isGlobal() || symbol.isPrimary()) {
                JsonObject item = new JsonObject();
                item.addProperty("name", symbol.getName(true));
                item.addProperty("address", symbol.getAddress().toString());
                item.addProperty("type", symbol.getSymbolType().toString());
                item.addProperty("source", symbol.getSource().toString());
                item.addProperty("external", symbol.isExternal());
                symbols.add(item);
            }
        }
        root.add("symbols", symbols);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File output = new File(args[0]);
        File parent = output.getParentFile();
        if (parent != null) parent.mkdirs();
        try (FileWriter writer = new FileWriter(output)) {
            gson.toJson(root, writer);
        }
        println("Wrote report to " + output.getAbsolutePath());
    }
}


