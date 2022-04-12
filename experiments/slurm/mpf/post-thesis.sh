# 4/10/2021 - exp log post-thesis
# bug fixes after thesis, msm, new jar : ts-chief-2.2.0-0410-post-thesis.jar

# test

# -------NOTES
## mEE experiments used 32 cpus
# slurm --partition=comp --qos=normal experiments max wall 7 days priority 50
# slurm --partition=rtqp --qos=rtq experiments max wall 48 hours priority 200
# slurm --partition=shortp --qos=shortq experiments max wall 30 min priority 200
# slurm --partition=rtqp --qos=irq experiments max wall 7 days priority 250 interruptable


python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=auto --cpu=4 --mem=200 --time=1:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf_test/k500e5-i1d2-n-depIndep-dimAll -c=ee:5 -dimDependency=independent -dimensions=all" --datasets=ERing,BasicMotions

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=auto --cpu=4 --mem=200 --time=1:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf_test/k500e5-i1d2-n-depDep-dimBeta1-3-sqrt -c=ee:5 -dimDependency=dependent -dimensions=beta:1:1" --datasets=ERing,BasicMotions

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=auto --cpu=4 --mem=200 --time=1:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf_test/k500e5-i1d2-n-depRand-dimBeta1-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=auto --cpu=4 --mem=200 --time=1:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf_test/k500e5-i1d2-n-depRand-dimUni1-D-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=ERing,BasicMotions

# ----------------------------------------------------------------------------------------------------------
## 26/8/2021 mpf experiments for thesis chapter 6

# 11-10-2021
# running order rand -> 2u, b13s, u11, u1D, b13, all, sqrt


python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#:DuckDuckGeese,PEMS-SF:PhonemeSpectra


# ------ todo

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r1 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimSqrt -c=ee:5 -dimDependency=random -dimensions=sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra


#----------------------- end 11/10/2021


# ----------------------------------------------------------------------------------------------------------
# -------------------start 12-10-2021
# slurm --partition=rtqp --qos=rtq experiments max wall 48 hours priority 200
# running order rand -> 2u, b13s, u11, u1D, b13, all, sqrt


python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r0r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra
#ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket:SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r0r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra
#ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket:SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r0r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra
#ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket:SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r0r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra
#ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket:SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r0r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra
#ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket:SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r0r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra
#ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket:SelfRegulationSCP1,SelfRegulationSCP2:EthanolConcentration,Heartbeat,LSST,PenDigits
#:DuckDuckGeese,PEMS-SF:PhonemeSpectra


# ------ todo

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r1r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimSqrt -c=ee:5 -dimDependency=random -dimensions=sqrt" --datasets=
#ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits
#,DuckDuckGeese,PEMS-SF,PhonemeSpectra


#----------------------- end 12/10/2021


# 12/10/2021 # slurm --partition=rtqp --qos=rtq experiments max wall 48 hours priority 200

#python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r5r --cpu=16 --mem=1000 --time=48:00:00 --partition=rtqp --qos=rtq --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=5 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits

# --partition=short --qos=shortq experiments max wall 30 min priority 250

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r6s --cpu=16 --mem=1000 --time=00:30:00 --partition=short --qos=shortq --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=6 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy:UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition:HandMovementDirection:StandWalkJump
#Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits


python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r6s --cpu=16 --mem=1000 --time=00:30:00 --partition=short --qos=shortq --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=6 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=HandMovementDirection:StandWalkJump:EthanolConcentration --dry

## TODO repeats
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r6s --cpu=16 --mem=1000 --time=00:30:00 --partition=short --qos=shortq --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=6 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --repeats=0,1:2,3:4 --datasets=HandMovementDirection:StandWalkJump




# ---------------------- start 13/10/2021  ---------------------------------------


# quick comp job high availability

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r032 --cpu=32 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-32cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra:EthanolConcentration,Heartbeat,LSST,PenDigits

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r032 --cpu=32 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-32cpu/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra:EthanolConcentration,Heartbeat,LSST,PenDigits

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r032 --cpu=32 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-32cpu/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra:EthanolConcentration,Heartbeat,LSST,PenDigits

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r032 --cpu=32 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-32cpu/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra:EthanolConcentration,Heartbeat,LSST,PenDigits

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r032 --cpu=32 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-32cpu/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra:EthanolConcentration,Heartbeat,LSST,PenDigits

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r032 --cpu=32 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-32cpu/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra:EthanolConcentration,Heartbeat,LSST,PenDigits















# ----- irq tests 16cpu or 32 cpu?
# slurm --partition=rtqp --qos=irq experiments max wall 7 days priority 250 interruptable
# running order rand -> 2u, b13s, u11, u1D, b13, all, sqrt


python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r0i --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-irq/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r0i --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-irq/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r0i --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-irq/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r0i --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-irq/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r0i --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-irq/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r0i --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-irq/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=DuckDuckGeese:PEMS-SF:PhonemeSpectra























# ---------------------- end 13/10/2021  --------------------------------------------



# ------------------------------------------------------------------------------------------------------------

# dimAll

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=i-all-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depIndep-dimAll -c=ee:5 -dimDependency=independent -dimensions=all" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits:DuckDuckGeese,PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=d-all-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depDep-dimAll -c=ee:5 -dimDependency=dependent -dimensions=all" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits:DuckDuckGeese,PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits:DuckDuckGeese,PEMS-SF:PhonemeSpectra

# dimBeta1-3-sqrt

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=i-b13s-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depIndep-dimBeta1-3-sqrt -c=ee:5 -dimDependency=independent -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=d-b13s-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depDep-dimBeta1-3-sqrt -c=ee:5 -dimDependency=dependent -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

# dimU1D

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=i-u1D-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depIndep-dimUni1-D -c=ee:5 -dimDependency=independent -dimensions=uniform:1:D" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=d-u1D-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depDep-dimUni1-D -c=ee:5 -dimDependency=dependent -dimensions=uniform:1:D" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF:PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF:PhonemeSpectra

# dimUni1-1

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=i-u11-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depIndep-dimUni1-1 -c=ee:5 -dimDependency=independent -dimensions=uniform:1:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=d-u11-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depDep-dimUni1-1 -c=ee:5 -dimDependency=dependent -dimensions=uniform:1:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

# dim2Uni0.5

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=i-2u-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depIndep-dim2Uni0.5 -c=ee:5 -dimDependency=independent -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=d-2u-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depDep-dim2Uni0.5 -c=ee:5 -dimDependency=dependent -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra


# dimBeta131

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=i-b131-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depIndep-dimBeta1-3-1 -c=ee:5 -dimDependency=independent -dimensions=beta:1:3:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=d-b131-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depDep-dimBeta1-3-1 -c=ee:5 -dimDependency=dependent -dimensions=beta:1:3:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra


# dimSqrt

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=i-b131-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depIndep-dimSqrt -c=ee:5 -dimDependency=independent -dimensions=sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=d-b131-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depDep-dimSqrt -c=ee:5 -dimDependency=dependent -dimensions=sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra

python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r0 --cpu=16 --mem=1000 --time=24:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimSqrt -c=ee:5 -dimDependency=random -dimensions=sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra










# ----------------------------------------- 1/11/2021 mpf-post-thesis-16cpu-rtqp started at 1-11-2021 10:41am


#faild jobs



# depRand-dimAll
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r1r --cpu=16 --mem=1000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r1r --cpu=16 --mem=1600 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimUni1-D
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r1r --cpu=16 --mem=1000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r1r --cpu=16 --mem=1600 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimBeta1-3-sqrt
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r1r --cpu=16 --mem=1000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r1r --cpu=16 --mem=1600 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimUni1-1
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r1r --cpu=16 --mem=1000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r1r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dim2Uni0.5
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r1r --cpu=16 --mem=1000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r1r --cpu=16 --mem=1600 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimBeta1-3-1
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r1r --cpu=16 --mem=1000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r1r --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=1 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu-rtqp/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra


# -depRand-dimSqrt
# TODO







# ----------------------------------------- 3/11/2021 mpf-post-thesis-16cpu started at 3-11-2021 10:41am
# same as rtqp jobs but on comp for staggering
# doing r2, rand

#faild jobs



# depRand-dimAll
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r4c --cpu=16 --mem=1000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r4c --cpu=16 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimAll -c=ee:5 -dimDependency=random -dimensions=all" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimUni1-D
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r4c --cpu=16 --mem=1000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u1D-r4c --cpu=16 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-D -c=ee:5 -dimDependency=random -dimensions=uniform:1:D" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimBeta1-3-sqrt
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r4c --cpu=16 --mem=1000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r4c --cpu=16 --mem=2000 --time=48:00:00 --partition=rtqp --qos=rtq  --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=3 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:5 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimUni1-1
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r4c --cpu=16 --mem=1000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-u11-r4c --cpu=16 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimUni1-1 -c=ee:5 -dimDependency=random -dimensions=uniform:1:1" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dim2Uni0.5
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r4c --cpu=16 --mem=1000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-2u-r4c --cpu=16 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dim2Uni0.5 -c=ee:5 -dimDependency=random -dimensions=two_uniform" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimBeta1-3-1
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r4c --cpu=16 --mem=1000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b131-r4c --cpu=16 --mem=2000 --time=48:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=4 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e5-i1d2-n-depRand-dimBeta1-3-1 -c=ee:5 -dimDependency=random -dimensions=beta:1:3:1" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra


# -depRand-dimSqrt
# TODO



#--------------------29/12 e:10----------------

# depRand-dimAll
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r0c --cpu=16 --mem=1000 --time=100:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=2 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e10-i1d2-n-depRand-dimAll -c=ee:10 -dimDependency=random -dimensions=all" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-all-r0c --cpu=16 --mem=2000 --time=100:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=2 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e10-i1d2-n-depRand-dimAll -c=ee:10 -dimDependency=random -dimensions=all" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra



# depRand-dimBeta1-3-sqrt
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r0c --cpu=16 --mem=1000 --time=100:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=2 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e10-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:10 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,EthanolConcentration,Heartbeat:SelfRegulationSCP1,SelfRegulationSCP2
python3 launcher.py --app=ts-chief-2.2.0-0410-post-thesis.jar --job=r-b13s-r0c --cpu=16 --mem=2000 --time=100:00:00 --args="-data=../data/ -archive=Multivariate2018_ts -overwriteResults=false -numRepeats=1 -repeatNo=2 -threads=0 -seed=0 -export=tfpc -norm=false -trees=500 -out=out/m3/mpf-post-thesis-16cpu/k500e10-i1d2-n-depRand-dimBeta1-3-sqrt -c=ee:10 -dimDependency=random -dimensions=beta:1:3:sqrt" --datasets=LSST,PenDigits,DuckDuckGeese:PEMS-SF:PhonemeSpectra





