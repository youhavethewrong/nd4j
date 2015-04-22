/*
 * Copyright 2015 Skymind,Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.nd4j.linalg.api.ops.impl.transforms;

import com.google.common.base.Function;
import org.nd4j.linalg.api.complex.IComplexNDArray;
import org.nd4j.linalg.api.complex.IComplexNumber;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.BaseTransformOp;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ComplexNDArrayUtil;

/**
 * Single ifft operation
 *
 * @author Adam Gibson
 */
public class VectorIFFT extends BaseTransformOp {


    private int fftLength;
    private int originalN = -1;

    public VectorIFFT(INDArray x, INDArray z,int fftLength) {
        super(x, z);
        this.fftLength = fftLength;
    }

    public VectorIFFT(INDArray x, INDArray z, int n,int fftLength) {
        super(x, z, n);
        this.fftLength = fftLength;
    }

    public VectorIFFT(INDArray x, INDArray y, INDArray z, int n,int fftLength) {
        super(x, y, z, n);
        this.fftLength = fftLength;
    }

    public VectorIFFT(INDArray x,int fftLength) {
        super(x);
        this.fftLength = fftLength;
    }



    @Override
    public String name() {
        return "ifft";
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, double other) {
        return origin;
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, float other) {
        return origin;
    }

    @Override
    public IComplexNumber op(IComplexNumber origin, IComplexNumber other) {
        return origin;
    }

    @Override
    public float op(float origin, float other) {
        return origin;
    }

    @Override
    public double op(double origin, double other) {
        return origin;
    }

    @Override
    public double op(double origin) {
        return origin;
    }

    @Override
    public float op(float origin) {
        return origin;
    }

    @Override
    public IComplexNumber op(IComplexNumber origin) {
        return origin;
    }

    @Override
    public Op opForDimension(int index, int dimension) {
        INDArray xAlongDimension = x.vectorAlongDimension(index, dimension);
        if (y() != null)
            return new VectorFFT(xAlongDimension, y.vectorAlongDimension(index, dimension), z.vectorAlongDimension(index, dimension), xAlongDimension.length(),fftLength);
        else
            return new VectorFFT(xAlongDimension, z.vectorAlongDimension(index, dimension), xAlongDimension.length(),fftLength);

    }

    @Override
    public void init(INDArray x, INDArray y, INDArray z, int n) {
        super.init(x, y, z, n);
        //ifft(x) = conj(fft(conj(x)) / length(x)
        IComplexNDArray ndArray = x instanceof IComplexNDArray ? (IComplexNDArray) x : Nd4j.createComplex(x);
        IComplexNDArray fft = (IComplexNDArray) Nd4j.getExecutioner().execAndReturn(new VectorFFT(ndArray.conj(),y,z,x.length(),fftLength));
        IComplexNDArray ret = fft.conj().divi(Nd4j.complexScalar(fftLength));
       //completely pass through
        this.z = originalN > 0 ? ComplexNDArrayUtil.truncate(ret, originalN, 0) : ret;
        this.x = this.z;
    }
}