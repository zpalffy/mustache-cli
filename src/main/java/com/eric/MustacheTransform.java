package com.eric;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;

public class MustacheTransform extends Command {

	@Parameter(description = "<mustache-template-file>")
	private List<String> templateFiles = Lists.newArrayList();

	@Parameter(names = { "-f", "--file", "--files" }, order = 0, description = "File(s) to load as context to the Mustache transformation.  "
			+ "These will be loaded as Strings and will be accessible via the associated variable names within the template.")
	private List<String> files = Lists.newArrayList();

	@Parameter(names = { "--var", "--vars" }, description = "The variable name(s) to use for any specified context files", order = 1)
	private List<String> variables = Lists.newArrayList("in");

	@Parameter(names = "--charset", description = "The charset to use when loading context files", order = 2)
	private String charset = StandardCharsets.UTF_8.name();

	@DynamicParameter(names = "-D", description = "String values to be added to the context")
	private Map<String, String> params = Maps.newHashMap();

	private List<String> jsonExtensions = Lists.newArrayList("json", "js");

	@Override
	protected String getProgramName() {
		return "mustache";
	}

	public static void main(String... args) throws IOException {
		Command.main(new MustacheTransform(), args);
	}

	@Override
	protected void validate(Collection<String> messages) {
		if (templateFiles.isEmpty()) {
			messages.add("<mustach-template-file> is required");
		}

		if (files.size() > variables.size()) {
			messages.add("Number of context files must match the number of variables.");
		}
	}

	private Map<String, Object> fillContext() throws IOException {
		Map<String, Object> context = new HashMap<String, Object>(params);
		List<String> fileNames = Lists.newArrayList(files.size());
		Object contents;

		for (int i = 0; i < files.size(); i++) {
			String filename = files.get(i);
			File f = new File(expandHomeDir(filename));
			fileNames.add(FilenameUtils.getBaseName(filename));
			contents = FileUtils.readFileToString(f, charset);

			if (FilenameUtils.isExtension(filename, jsonExtensions)) {
				// TODO load as json:
			}

			context.put(variables.get(i), contents);
		}

		context.put("fileNames", fileNames);
		return context;
	}

	public void run() throws IOException {
		MustacheFactory factory = new DefaultMustacheFactory();
		Map<String, Object> context = fillContext();
		Writer writer = new PrintWriter(System.out);

		for (String template : templateFiles) {
			factory.compile(new FileReader(expandHomeDir(template)), template).execute(writer, context);
		}

		writer.flush();
	}
}