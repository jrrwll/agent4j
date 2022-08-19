package org.dreamcat.common.agent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Jerry Will
 * @version 2022-04-25
 */
public class DnsTest {

    public static void main(String[] args) throws InterruptedException {
        for (; ; ) {
            try {
                System.out.println(InetAddress.getByName("example.com"));
                System.out.println(InetAddress.getByName("stackoverflow.com"));
                System.out.println(InetAddress.getByName("spring.io"));
                System.out.println(".........");
            } catch (UnknownHostException e) {
                System.err.println(e.getMessage());
            }
            Thread.sleep(3000);
        }
    }
}
/*
cd common-agent/build/libs
md -p org/dreamcat/common/agent
old_pwd=`pwd -P`
cd ../../src/test/java
javac org/dreamcat/common/agent/DnsTest.java
mv org/dreamcat/common/agent/DnsTest.class $old_pwd/org/dreamcat/common/agent
cd $old_pwd
java org.dreamcat.common.agent.DnsTest

test_pid=`jps | grep DnsTest | cut -d' ' -f1`
java -cp $JAVA_HOME/lib -jar common-agent-0.2.1-all.jar --attach $test_pid

*/