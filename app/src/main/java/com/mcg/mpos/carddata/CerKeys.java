package com.mcg.mpos.carddata;

import com.mcg.mpos.utils.StringEncode;

public class CerKeys {
	
	private byte[] CAModulus;
	
	private byte[] CAExponent;
	
	public CerKeys() {
		CAModulus =  StringEncode.hexDecode("CCDBA686E2EFB84CE2EA01209EEB53BEF21AB6D353274FF8391D7035D76E2156CAEDD07510E07DAFCACABB7CCB0950BA2F0A3CEC313C52EE6CD09EF00401A3D6CC5F68CA5FCD0AC6132141FAFD1CFA36A2692D02DDC27EDA4CD5BEA6FF21913B513CE78BF33E6877AA5B605BC69A534F3777CBED6376BA649C72516A7E16AF85");
		CAExponent = StringEncode.hexDecode("010001");
	}

	public byte[] getCAModulus() {
		return CAModulus;
	}

	public void setCAModulus(byte[] cAModulus) {
		CAModulus = cAModulus;
	}

	public byte[] getCAExponent() {
		return CAExponent;
	}

	public void setCAExponent(byte[] cAExponent) {
		CAExponent = cAExponent;
	}
}
