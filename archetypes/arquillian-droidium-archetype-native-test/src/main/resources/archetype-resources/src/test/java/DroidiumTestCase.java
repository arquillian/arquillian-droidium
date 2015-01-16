package ${package};

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.native_.api.Instrumentable;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * My Arquillian Droidium test #1
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DroidiumTestCase {

    @Drone
    private WebDriver driver;

    @ArquillianResource
    private AndroidDevice device;
    
    @Deployment
    @Instrumentable
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("your-apk-under-test"));
    }

    @Test
    @InSequence(1)
    public void test01() {
        // your tests
    }
}
