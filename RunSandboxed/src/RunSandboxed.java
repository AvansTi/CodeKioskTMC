import java.awt.AWTPermission;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.MalformedURLException;
import java.net.NetPermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.Enumeration;
import java.util.PropertyPermission;

public class RunSandboxed {
	static String mainClass;
	public static void main(String[] args) {

		Runnable unprivileged = new Runnable() {
			public void run() {
				try {
					System.out.println("Running " + mainClass);

					System.out.println("Path: " + new File(".").getAbsolutePath());


					URL url = null;
					try {
						url = new File("fxgraphics2d-1.5.jar").toURI().toURL();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					ClassLoader systemClassloader = ClassLoader.getSystemClassLoader();
					final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url}, systemClassloader);

					try {
						Field scl = ClassLoader.class.getDeclaredField("scl");
						scl.setAccessible(true); // Set accessible
						scl.set(null, urlClassLoader); // Update it to your class loader

					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}


					Class<?> c = urlClassLoader.loadClass(mainClass);

					//Class<?> c = Class.forName(mainClass);
					Method main = c.getMethod("main", String[].class);
					String[] args = new String[0];
					main.invoke(null, (Object)args);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
					e.printStackTrace();
				} catch(InvocationTargetException e)
				{
					System.out.println(e.getTargetException().getMessage());
					e.getTargetException().printStackTrace();
				}
				
			}
		};
		
		if(args.length == 0)
		{
			System.err.println("Please specify a main");
			System.exit(0);
		}
		

		String main = args[0];
		
		
		try {
			addSoftwareLibrary(new File(System.getProperty("user.dir")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*try {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			
			Enumeration<URL> urls = classloader.getResources("META-INF/MANIFEST.MF");
			while(urls.hasMoreElements())
			{
				URL u = urls.nextElement();

				InputStream in = u.openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while((line = reader.readLine()) != null)
				{
					if(line.startsWith("Main-Class: "))
					{
						if(!line.substring(12).trim().equals("RunSandboxed"))
						{
							mainClass = line.substring(12).trim();
							System.out.println("Found main class: " + mainClass);
						}
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/

			mainClass = main;
		
		
		
		// Set the most strict permissions.
		Permissions p = new Permissions();
		p.add(new PropertyPermission("java.util.*", "read"));
		p.add(new PropertyPermission("javax.accessibility.*", "read"));
		p.add(new PropertyPermission("javax.swing.*", "read"));
		p.add(new PropertyPermission("jdk.internal.*", "read"));
		p.add(new PropertyPermission("java.awt.*", "read"));
		p.add(new PropertyPermission("javaplugin.*", "read"));
		p.add(new PropertyPermission("os.name", "read"));
		p.add(new PropertyPermission("user.dir", "read"));
		p.add(new PropertyPermission("os.version", "read"));
		p.add(new PropertyPermission("awt.image.*", "read"));
		p.add(new PropertyPermission("javawebstart.*", "read"));
		p.add(new PropertyPermission("sun.awt.*", "read"));
		p.add(new PropertyPermission("sun.java.*", "read"));
		p.add(new PropertyPermission("awt.appletWarning", "read"));
		p.add(new PropertyPermission("awt.toolkit", "read"));
		p.add(new PropertyPermission("awt.nativeDoubleBuffering", "read"));
		p.add(new PropertyPermission("awt.useSystemAAFontSettings", "read"));
		p.add(new PropertyPermission("AWT.EventQueueClass", "read"));
		p.add(new PropertyPermission("java.home", "read"));
		p.add(new PropertyPermission("java.version", "read"));
		p.add(new PropertyPermission("sun.java2d.*", "read"));
		p.add(new PropertyPermission("sun.perflog", "read"));
		p.add(new PropertyPermission("browser", "read"));
		p.add(new PropertyPermission("sun.awt.enableExtraMouseButtons", "read,write"));
		p.add(new PropertyPermission("swing.*", "read"));
		p.add(new PropertyPermission("*", "read"));
		p.add(new PropertyPermission("sun.misc.*", "read"));
	
		p.add(new ReflectPermission("suppressAccessChecks"));
		
		String path = System.getProperty("java.home");
		p.add(new FilePermission(path + "/bin/*", "read"));
		p.add(new FilePermission("C:\\Program Files\\Java\\jdk1.8.0_152\\jre\\lib\\ext\\jfxrt.jar", "read"));
		p.add(new FilePermission(path + "/lib/*", "read"));
		p.add(new FilePermission(path + "/lib/fonts/*", "read"));
		p.add(new FilePermission(path + "/*", "read"));
		p.add(new FilePermission("c:/Windows/Fonts", "read"));
		p.add(new FilePermission("c:/Windows/Fonts/*", "read"));
		p.add(new FilePermission("c:/Users/johan/Desktop/*", "read"));
		p.add(new FilePermission("c:/Users/johan/*", "read"));
		p.add(new FilePermission("c:/Users/jgctalbo/*", "read"));
		p.add(new FilePermission("c:/Users/jgctalbo/Desktop/*", "read"));


		path = System.getProperty("user.dir");
		p.add(new FilePermission(path, "read"));
		p.add(new FilePermission(path + "/*", "read"));

		p.add(new FilePermission("*.jar", "read"));
		p.add(new FilePermission("*.class", "read"));
		p.add(new FilePermission("d:/Avans/CodeKioskTMC/sandbox/*", "read"));
		p.add(new FilePermission("d:/Avans/CodeKioskTMC/sandbox_lib/*", "read"));
		p.add(new FilePermission("D:\\Avans\\CodeKioskTMC\\sandbox\\out\\*", "read"));

		p.add(new FilePermission("D:\\Avans\\CodeKioskTMC\\sandbox\\out\\org\\jfree\\fx\\*", "read"));



		//p.add(new FilePermission(jarfile, "read"));

		p.add(new NetPermission("specifyStreamHandler"));
		p.add(new RuntimePermission("createClassLoader"));
		p.add(new RuntimePermission("getProtectionDomain"));
		p.add(new RuntimePermission("loadLibrary.awt"));
		p.add(new RuntimePermission("getenv.DISPLAY"));
		p.add(new RuntimePermission("getenv.JAVA2D_USEPLATFORMFONT"));
		p.add(new RuntimePermission("modifyThreadGroup"));
		p.add(new RuntimePermission("modifyThread"));
		p.add(new RuntimePermission("setContextClassLoader"));
		p.add(new RuntimePermission("accessClassInPackage.sun.reflect.misc"));
		p.add(new RuntimePermission("accessClassInPackage.sun.reflect"));
		p.add(new RuntimePermission("getClassLoader"));

		p.add(new RuntimePermission("accessDeclaredMembers"));
		p.add(new RuntimePermission("accessClassInPackage.sun.awt"));
		p.add(new RuntimePermission("exitVM.0"));

		p.add(new RuntimePermission("fileSystemProvider")); //TODO: remove
		p.add(new RuntimePermission("loadLibrary.net")); //TODO: remove
		p.add(new RuntimePermission("loadLibrary.t2k")); //TODO: remove
		p.add(new RuntimePermission("loadLibrary.nio")); //TODO: remove
		p.add(new RuntimePermission("loadLibrary.jpeg")); //TODO: remove
		p.add(new RuntimePermission("loadLibrary.fontmanager")); //TODO: remove???
		p.add(new RuntimePermission("accessClassInPackage.sun.security.*")); //TODO: remove???

		
		
		
		p.add(new SecurityPermission("getProperty.jdk.jar.disabledAlgorithms", "read"));
		for(int i = 1; i < 20; i++)
			p.add(new SecurityPermission("getProperty.security.provider." + i, "read"));
		p.add(new SecurityPermission("getProperty.securerandom.source", "read"));
		p.add(new SecurityPermission("putProviderProperty.*", "read"));
		
		p.add(new AWTPermission("listenToAllAWTEvents"));
		p.add(new AWTPermission("showWindowWithoutWarningBanner"));

		
		
		
	
		Sandbox.confine(unprivileged.getClass(), p);
		Thread t = new Thread(unprivileged);
		t.start();

		long time = System.currentTimeMillis()+30000;
		try {
			t.join(10000);

			//Thread.sleep(time-System.currentTimeMillis());
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("Killed...");
		System.exit(0);
	}
	
	
	private static void addSoftwareLibrary(File file) throws Exception {
	    Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
	    method.setAccessible(true);
	    method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{file.toURI().toURL()});
	}
}
