import javax.json.*;
import javax.net.ssl.HttpsURLConnection;
import javax.tools.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
	public static Path mainFile;

	public static void main(String[] args) throws IOException, InterruptedException {

		QueueListener queue = new QueueListener();
		while(true) {
			int id = queue.nextSubmission();
		//	int id = 3417499;
			System.out.println("Starting submission " + id);

			queue.getSubmissionZip(id);

			try {
				try {
					cleanSandbox("sandbox/" + id);
				} catch (NoSuchFileException e) {
				}
				try {
					Files.createDirectory(Paths.get("sandbox/" + id));
				} catch (FileAlreadyExistsException e) {
				}
				unzip("submissions/" + id + ".zip", "sandbox/" + id);
				compile(id);

				String mainClass = mainFile.getFileName().toString();
				mainClass = mainClass.substring(0, mainClass.lastIndexOf("."));
				Path src = Files.walk(Paths.get("sandbox/" + id)).filter(p -> p.getFileName().toString().equals("src")).findFirst().get();
				System.out.println("Main file is " + mainClass);
				System.out.println("src path is " + src);

				Path jar = Paths.get("RunSandboxed.jar").toAbsolutePath();
				System.out.println(jar);


				ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", ".;../../../../sandbox_lib/fxgraphics2d-1.5.jar", "-jar", jar.toString(), mainClass);
				processBuilder.directory(src.toFile());
				Process p = processBuilder.start();
/*				BufferedReader br = new BufferedReader(
						new InputStreamReader(
								p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}*/
				p.waitFor(10, TimeUnit.SECONDS);

				p.destroy();
				p.destroyForcibly();
				p.waitFor();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			//break;
		}
	}


	private static void compile(int id) throws IOException {
		Path src = Files.walk(Paths.get("sandbox/" + id)).filter(p -> p.getFileName().toString().equals("src")).findFirst().get();
		Path out = Paths.get("sandbox/" + id).resolve("out");
		Path lib = Paths.get("sandbox_lib");
		Files.createDirectories(out);

		System.out.println("src is at " + src);
		System.out.println("out is at " + out);

		Files.walk(src).filter(file -> file.toString().endsWith(".java")).forEach(file -> {

			try {
				String code = String.join("\n", Files.readAllLines(file));
				code = code.replace(" ", "");
				code = code.replace("\t", "");

				if(code.contains("publicstaticvoidmain")) {
					mainFile = file;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(compile(file.toString(), src, out, lib));
		});


	}

	private static String compile(String file, Path src, Path out, Path lib) {
		System.out.println("Compiling " + file);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		//Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFiles));
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(new String[]{file}));

		String classpath = lib.toString() + "\\*" + ";" + src.toString();

		List<String> optionList = new ArrayList<String>();

		try {
//			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File[]{out.toFile()}));

			List<File> cp = Files.list(lib).map(e -> e.toFile()).collect(Collectors.toList());
			cp.add(out.toFile());
			cp.add(src.toFile());

			fileManager.setLocation(StandardLocation.CLASS_PATH, cp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);
		if (!task.call()) {
			return "Compile error: \n" + diagnostics.getDiagnostics();
		}

		return "Compile success";
	}



	private static void cleanSandbox(String outPath) throws IOException {
		Files.walkFileTree(Paths.get(outPath), new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//				System.out.println("Deleting " + file);
//				try {
					Files.delete(file);
//					Thread.sleep(100);
//				} catch(Exception e)
//				{
//					e.printStackTrace();
//				}
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//				System.out.println("Deleting dir " + dir);
				for(int i = 0; i < 10; i++) {
					try {
						Files.delete(dir);
						break;
					} catch (Exception e) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void unzip(String zipFile, String outPath) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(new File(outPath), zipEntry);
			if(zipEntry.isDirectory()) {
				try {
					Files.createDirectories(Paths.get(newFile.getAbsolutePath()));
				} catch(Exception e) {}
			}
			else {
				for(int i = 0; i < 100; i++)
				{
					try {
						FileOutputStream fos = new FileOutputStream(newFile);

						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
						break;
					} catch(IOException e)
					{
						System.out.println("Could not write " + newFile);
					}
				}

			}
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}
	private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}


}
