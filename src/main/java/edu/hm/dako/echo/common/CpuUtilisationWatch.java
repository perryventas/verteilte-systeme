package edu.hm.dako.echo.common;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;


public class CpuUtilisationWatch {

    private static final OperatingSystemMXBean osbean = (OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean();
    private static final int nCPUs = osbean.getAvailableProcessors();

    private Long startWallclockTime;
    private Long startCpuTime;

    public CpuUtilisationWatch() {
        startWallclockTime = System.nanoTime();
        startCpuTime = osbean.getProcessCpuTime();
    }

    public float getAverageCpuUtilisation() {
        float wallclockTimeDelta = System.nanoTime() - startWallclockTime;
        float cpuTimeDelta = osbean.getProcessCpuTime() - startCpuTime;
        cpuTimeDelta = Math.max(cpuTimeDelta, 1);

        return (cpuTimeDelta / (float) nCPUs) / wallclockTimeDelta;
    }
}
