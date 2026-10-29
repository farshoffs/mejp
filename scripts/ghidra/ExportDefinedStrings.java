// Export strings defined by Ghidra's analyzers for offline review.
//@category MyJPJ

import java.io.File;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.DataIterator;

public class ExportDefinedStrings extends GhidraScript {
    @Override
    public void run() throws Exception {
        String[] args = getScriptArgs();
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: ExportDefinedStrings <output.json>");
        }

        JsonArray strings = new JsonArray();
        DataIterator iterator = currentProgram.getListing().getDefinedData(true);
        while (iterator.hasNext()) {
            Data data = iterator.next();
            if (monitor.isCancelled()) break;
            Object rawValue = data.getValue();
            if (!(rawValue instanceof String)) continue;
            JsonObject item = new JsonObject();
            item.addProperty("address", data.getAddress().toString());
            item.addProperty("length", data.getLength());
            item.addProperty("value", (String) rawValue);
            strings.add(item);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File output = new File(args[0]);
        File parent = output.getParentFile();
        if (parent != null) parent.mkdirs();
        try (FileWriter writer = new FileWriter(output)) {
            gson.toJson(strings, writer);
        }
        println("Wrote " + strings.size() + " strings to " + output.getAbsolutePath());
    }
}


