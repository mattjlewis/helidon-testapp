package uk.mattjlewis.helidon.testapp.jandexindex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

@Mojo(
		name = "jandexindex",
		defaultPhase = LifecyclePhase.PROCESS_CLASSES,
		threadSafe = true)
public class JandexIndexGoal extends AbstractMojo {
	@Parameter
	private String myProp;
	
	@Parameter(defaultValue = "${project.build.outputDirectory}")
	private String classesDir;

	/**
	 * Process the classes found in these file-sets, after considering the specified includes and excludes, if any. The
	 * format is: <br/>
	 *
	 * <pre>
	 * <code>
	 * &lt;fileSets&gt;
	 *   &lt;fileSet&gt;
	 *     &lt;directory&gt;path-or-expression&lt;/directory&gt;
	 *     &ltincludes&gt;
	 *       &lt;include&gt;some/thing/*.good&lt;/include&gt;
	 *     &lt;includes&gt;
	 *     &lt;excludes&gt;
	 *       &lt;exclude&gt;some/thing/*.bad&lt;/exclude&gt;
	 *     &lt;/excludes&lt;
	 *   &lt;/fileSet&gt;
	 * &lt;/fileSets&gt;
	 * </code>
	 * </pre>
	 *
	 * <br>
	 * <em>NOTE: Standard globbing expressions are supported in includes/excludes.</em>
	 */
	@Parameter
	private FileSet[] fileSets;

	/**
	 * If true, construct an implied file-set using the target/classes directory, and process the classes there.
	 */
	@Parameter(defaultValue = "true")
	private boolean processDefaultFileSet;

	/**
	 * Print verbose output (debug output without needing to enable -X for the whole build)
	 */
	@Parameter(defaultValue = "false")
	private boolean verbose;

	/**
	 * The name of the index file. Default's to 'jandex.idx'
	 *
	 * @parameter default-value="jandex.idx"
	 */
	@Parameter(defaultValue = "jandex.idx")
	private String indexName = "jandex.idx";

	/**
	 * Skip execution if set.
	 */
	@Parameter(defaultValue = "false")
	private boolean skip;

	private Log log;

	public JandexIndexGoal() {
	}

	private boolean isVerboseLogging() {
		return verbose || getLog().isDebugEnabled();
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(final boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(final boolean skip) {
		this.skip = skip;
	}

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public void setLog(final Log log) {
		this.log = log;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Jandex execution skipped.");
			return;
		}
		
		final List<FileSet> file_set_list = new ArrayList<>();
		if (fileSets != null) {
			for (FileSet fs : fileSets) {
				getLog().debug("Configured fileSet dir: " + fs.getDirectory());
				fs.setIncludes(Collections.singletonList("**/*.class"));
				file_set_list.add(fs);
			}
		}

		if (processDefaultFileSet) {
			boolean found = false;
			for (final FileSet fileset : file_set_list) {
				if (fileset.getDirectory().equals(classesDir)) {
					found = true;
				}
			}

			if (!found) {
				final FileSet fs = new FileSet();
				fs.setDirectory(classesDir);
				fs.setIncludes(Collections.singletonList("**/*.class"));

				file_set_list.add(fs);
			}
		}

		final Indexer indexer = new Indexer();
		getLog().debug("Scanning...");
		for (final FileSet fileset : file_set_list) {
			final Path fileset_path = Paths.get(fileset.getDirectory());
			getLog().debug("Scanning " + fileset.getDirectory() + ", " + fileset_path.toAbsolutePath());
			if (!fileset_path.toFile().exists()) {
				getLog().debug("[SKIP] Cannot process fileset in directory: " + fileset.getDirectory()
						+ ". Directory " + fileset_path + " does not exist!");
				continue;
			}

			final DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(fileset_path.toAbsolutePath().toString());

			final List<String> includes = fileset.getIncludes();
			if (includes != null) {
				scanner.setIncludes(includes.toArray(new String[] {}));
			}

			final List<String> excludes = fileset.getExcludes();
			if (excludes != null) {
				scanner.setExcludes(excludes.toArray(new String[] {}));
			}

			scanner.scan();
			final String[] files = scanner.getIncludedFiles();

			for (final String file : files) {
				if (file.endsWith(".class")) {
					try (FileInputStream fis = new FileInputStream(fileset_path.resolve(file).toFile())) {
						final ClassInfo class_info = indexer.index(fis);
						if (isVerboseLogging() && class_info != null) {
							getLog().info("Indexed " + class_info.name() + " (" + class_info.annotations().size()
									+ " annotations)");
						}
					} catch (IOException e) {
						throw new MojoExecutionException(e.getMessage(), e);
					}
				}
			}
		}

		final Index index = indexer.complete();
		
		// FIXME Parameterise output_dir
		String output_dir = "target/classes";
		final File idx = new File(output_dir + "/META-INF/" + indexName);
		idx.getParentFile().mkdirs();

		try (FileOutputStream indexOut = new FileOutputStream(idx)) {
			new IndexWriter(indexOut).write(index);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

		// Print out the classes in the index
		getLog().debug("*** Known classes:");
		for (ClassInfo class_info : index.getKnownClasses()) {
			getLog().debug(class_info.name().toString());
		}
	}
}
