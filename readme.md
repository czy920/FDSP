# Implementation of FDSP, CONC-FDSP & BFS-FDSP
## Problem generation
Please refer to
https://github.com/dyc941126/DCOPBenchmarks

## Run algorithms
Pass the following arguments to main class ``org.infrastructure.main.Solve``:
```bash
-p            path to problem file
-am am_file   path to agent_manifest.xml
-a algo       acceleration algorithm [FDSP|CONC|BFS|GD2P|FDSP|ST-GD2P|PTS]
optional arguments
-c cycle      numer of cycles (iterations) to run (default=2000)
-t step_size  step size for ST-GD2P & PTS
-cr criterion sorting criterion for PTS [MAX|MEAN|QUANTILE|HINDEX]
-s depth      sorting depth for PTS
-type type    round-robin mechanism for CONC [STEP|UPDATE]   
```
Example
```
java -jar FDSP.jar -p foo.xml -am am.xml -a BFS -c 1000 
```