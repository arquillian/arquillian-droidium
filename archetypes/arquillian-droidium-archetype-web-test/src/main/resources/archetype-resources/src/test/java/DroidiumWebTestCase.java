package ${package};

import java.io.File;
import java.net.URL;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.android.AndroidDriver;

/**
 * My first Arquillian Droidium web test
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DroidiumWebTestCase {

    @Deployment(name = "jbossas", testable = false)
    @TargetsContainer("jbossas")
    public static Archive<?> getDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/jboss-as-helloworld.war"));
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("jbossas")
    public void test01(@Drone AndroidDriver driver, @ArquillianResource URL deploymentURL) {

        // get deployment URL
        driver.get(deploymentURL.toString());

        // your tests ...
    }
}
