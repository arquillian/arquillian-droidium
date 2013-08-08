package ${package};

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
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
public class SelendroidTestAppTestCase {

    @Deployment(name = "android")
    @TargetsContainer("android")
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("your-apk-under-test"));
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("android")
    public void test01(@ArquillianResource AndroidDevice android, @Drone WebDriver driver) {
        // your tests
    }
}
