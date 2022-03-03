package dotnet54.measures;

public enum Measure {
	euc, euclidean,
	dtwf, // full window
	dtw, // window selected by the method
	dtwr, // random window
	dtwcv, // cv window
	ddtwf,
	ddtw,
	ddtwr,
	ddtwcv,
	wdtw,
	wddtw,
	erp,
	lcss,
	msm,
	twe,

	// multivariate -- dependent
	euc_d,
	dtwf_d,
	dtw_d,
	ddtwf_d,
	ddtw_d,
	wdtw_d,
	wddtw_d,
	lcss_d,
	erp_d,
	msm_d,
	twe_d,

	// experimental -- other implementations
	equality,
	basicDTW,
	dtwDistance, 
	dtwDistanceEfficient,
	pdtw, scdtw,  francoisDTW, smoothDTW,dca_dtw,
}
