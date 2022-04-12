

# -----------------------------------------------------------------------------------------------------------
# KAIS - Revision 1
# 30-01-2022 - resample experiments
# -----------------------------------------------------------------------------------------------------------


# slurm notes
# ---------------------------------------------------------------------------------------------------------
# show_cluster, show_jobs, scancel -u ashi32, squeue -u ashi32
# interactive smux new-session --time=0-00:30:00 | smux list-sessions | smux attach-session <jobid>

# qos : https://docs.massive.org.au/M3/slurm/using-qos.html
# normal 7 days, 200 cpus, priority 50, partition comp
# rtq 48 hr, 72 cpus, priority 200, partition rtqp
# irq 7 days, no limit cpus, priority 200, interruptable, partition rtqp
# shortq 30 min, no limit cpus, priority 280, partition short


# datasets
# ---------------------------------------------------------------------------------------------------------
# ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,
# FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:
# SelfRegulationSCP1,SelfRegulationSCP2:
# LSST,PenDigits,DuckDuckGeese:
# PEMS-SF:
# PhonemeSpectra

# tests
# ---------------------------------------------------------------------------------------------------------
python3 launcher.py -train=true -test=false -j=test-comp -cpu=2 -mem=500 -time=00:30:00 -part=comp -qos=normal -o=rev1/ -norm=false -fold=0 -pi=1 -pd=2 -dp=false,true -p=100:0,100,1 -m=euc,dtwf,dtwr,ddtwf,ddtwr,wdtw,wddtw,lcss,erp,msm,twe -d=ERing -dry
python3 launcher.py -train=true -test=false -j=test-rtq -cpu=16 -mem=500 -time=00:30:00 -part=rtqp -qos=rtq -o=rev1/ -norm=false -fold=0 -pi=1 -pd=2 -dp=false,true -p=100:0,100,1 -m=euc,dtwf,dtwr,ddtwf,ddtwr,wdtw,wddtw,lcss,erp,msm,twe -d=ERing -dry



# EXPERIMENTS
# ---------------------------------------------------------------------------------------------------------

# 31-01-2022 ------------------ FOLD 1 TRAIN

# all fold 1 train jobs queued at 31-01-2022 9:36pm, 150 jobs from 24585309 to 24585458
python3 launcher.py -train=true -test=false -j=f1-12-n -cpu=16 -mem=1000 -time=100:00:00 -part=comp -qos=normal -o=rev1/ -norm=false -fold=1 -pi=1 -pd=2 -dp=false:true -p=100:0,100,1 -m=euc,dtwf,dtwr:ddtwf,ddtwr:wdtw,wddtw:lcss,erp,twe:msm -d=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2:LSST,PenDigits,DuckDuckGeese
python3 launcher.py -train=true -test=false -j=f1-12-n-PEM -cpu=16 -mem=1000 -time=100:00:00 -part=comp -qos=normal -o=rev1/ -norm=false -fold=1 -pi=1 -pd=2 -dp=false:true -p=100:0,100,1 -m=euc,dtwf:dtwr:ddtwf:ddtwr:wdtw:wddtw:lcss:erp:twe:msm -d=PEMS-SF
python3 launcher.py -train=true -test=false -j=f1-12-n-PS -cpu=16 -mem=1000 -time=100:00:00 -part=comp -qos=normal -o=rev1/ -norm=false -fold=1 -pi=1 -pd=2 -dp=false:true -p=20:0,100,1 -m=euc,dtwf:dtwr:ddtwf:ddtwr:wdtw:wddtw:lcss:erp:twe:msm -d=PhonemeSpectra



# 31-01-2022 ------------------ FOLD 2 TRAIN

python3 launcher.py -train=true -test=false -j=f1-12-n -cpu=16 -mem=1000 -time=100:00:00 -part=comp -qos=normal -o=rev1/ -norm=false fold=2 -pi=1 -pd=2 -dp=false:true -p=100:0,100,1 -m=euc,dtwf,dtwr:ddtwf,ddtwr:wdtw,wddtw:lcss,erp,twe:msm -d=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2:LSST,PenDigits,DuckDuckGeese
python3 launcher.py -train=true -test=false -j=f1-12-n-PEM -cpu=16 -mem=1000 -time=100:00:00 -part=comp -qos=normal -o=rev1/ -norm=false fold=2 -pi=1 -pd=2 -dp=false:true -p=100:0,100,1 -m=euc,dtwf:dtwr:ddtwf:ddtwr:wdtw:wddtw:lcss:erp:twe:msm -d=PEMS-SF
python3 launcher.py -train=true -test=false -j=f1-12-n-PS -cpu=16 -mem=1000 -time=100:00:00 -part=comp -qos=normal -o=rev1/ -norm=false fold=2 -pi=1 -pd=2 -dp=false:true -p=20:0,100,1 -m=euc,dtwf:dtwr:ddtwf:ddtwr:wdtw:wddtw:lcss:erp:twe:msm -d=PhonemeSpectra
