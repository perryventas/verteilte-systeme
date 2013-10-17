package edu.hm.dako.echo.benchmarking;

/**
 * Schnittstelle zum Starten eines Benchmarks
 *
 * @author Mandl
 */
public interface BenchmarkingStartInterface {

    /**
     * Methode fuehrt den Benchhmark aus
     *
     * @param parm      Input-Parameter
     * @param clientGui Schnittstelle zur GUI
     */
    public void executeTest(UserInterfaceInputParameters parm, BenchmarkingClientUserInterface clientGui);
}
