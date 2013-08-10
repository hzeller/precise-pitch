/* DyWaPitchTrack
 
   Dynamic Wavelet Algorithm Pitch Tracking library
   Released under the MIT open source licence
 
   Copyright (c) 2010 Antoine Schmitt
   Translated to Java by Henner Zeller

   Based on
   "Real-Time Time-Domain Pitch Tracking Using Wavelets"
   http://courses.physics.illinois.edu/phys406/NSF_REU_Reports/2005_reu/Real-Time_Time-Domain_Pitch_Tracking_Using_Wavelets.pdf

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:
 
   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.
 
   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.
*/
package net.zllr.precisepitch;

import android.os.Build;

import java.util.Arrays;

// Dynamic Wavelet pitch tracking.
public final class DyWaPitchTrack {
    // Emulator can only do 8kHz. Device 44.1k.
    public static final int kSampleRateHz
        = "sdk".equals(Build.PRODUCT) ? 8000 : 44100;

    // Returns a suggested sample count needed to properly detect the given
    // minimum frequency. This is the input to the constructor, and the number
    // of sample to be supplied to computePitch()
    public static int suggestedSamplecount(int minFrequency) {
	final int minSamples = (int) (3 * kSampleRateHz / minFrequency);
        int minSamplesPower2 = 512;
        while (minSamplesPower2 < minSamples) {
            minSamplesPower2 <<= 1;
        }
        return minSamplesPower2;
    }

    public DyWaPitchTrack(int samplecount) {
        this.samplecount = samplecount;
        distances = new int[samplecount];
        mins = new int[samplecount];
        maxs = new int[samplecount];
        pitchConfidence = -1;
        prevPitch = -1.0;
    }

    // Compute pitch given the new set of samples. This is stateful, it depends
    // on previous calls to this method to help filter out glitches.
    public double computePitch(double samples[]) {
        final double raw_pitch = computeWaveletPitch(samples);
        return dynamicTracked(raw_pitch);
    }

    // Returns a pitch from applying the wavelet algorithm; stateless.
    public double computeWaveletPitch(double samples[]) {
	double result = 0.0;
	
	double si, si1;
	
	int curSamNb = samplecount;
	
	int nbMins, nbMaxs;
	
	// algorithm parameters
	int maxFLWTlevels = 6;
	double maxF = 3000.0;
	int differenceLevelsN = 3;
	double maxThresholdRatio = 0.75;
	
	double amplitudeThreshold;
	double theDC = 0.0;
	
	{ 
            // compute ampltitudeThreshold and theDC
            //first compute the DC and maxAMplitude
            double maxValue = 0.0;
            double minValue = 0.0;
            for (int i = 0; i < samplecount;i++) {
                si = samples[i];
                theDC = theDC + si;
                if (si > maxValue) maxValue = si;
                if (si < minValue) minValue = si;
            }
            theDC = theDC/samplecount;
            maxValue = maxValue - theDC;
            minValue = minValue - theDC;
            double amplitudeMax = (maxValue > -minValue ? maxValue : -minValue);
            
            amplitudeThreshold = amplitudeMax*maxThresholdRatio;
	}
	
	// levels, start without down-sampling..
	int curLevel = 0;
	double curModeDistance = -1.;
	
        for (;;) {
            // delta
            double delta = kSampleRateHz / (power2(curLevel) * maxF);
		
            if (curSamNb < 2)
                break;
		
            // compute the first maximums and minumums after zero-crossing
            // store if greater than the min threshold
            // and if at a greater distance than delta
            double dv, previousDV = -1000;
            nbMins = nbMaxs = 0;   
            int lastMinIndex = -1000000;
            int lastmaxIndex = -1000000;
            boolean findMax = false;
            boolean findMin = false;
            for (int i = 2; i < curSamNb; i++) {
                si = samples[i] - theDC;
                si1 = samples[i-1] - theDC;
			
                if (si1 <= 0 && si > 0) findMax = true;
                if (si1 >= 0 && si < 0) findMin = true;
			
                // min or max ?
                dv = si - si1;
			
                if (previousDV > -1000) {
				
                    if (findMin && previousDV < 0 && dv >= 0) { 
                        // minimum
                        if (Math.abs(si) >= amplitudeThreshold) {
                            if (i > lastMinIndex + delta) {
                                mins[nbMins++] = i;
                                lastMinIndex = i;
                                findMin = false;
                            }
                        }
                    }
				
                    if (findMax && previousDV > 0 && dv <= 0) {
                        // maximum
                        if (Math.abs(si) >= amplitudeThreshold) {
                            if (i > lastmaxIndex + delta) {
                                maxs[nbMaxs++] = i;
                                lastmaxIndex = i;
                                findMax = false;
                            }
                        }
                    }
                }
			
                previousDV = dv;
            }
		
            if (nbMins == 0 && nbMaxs == 0) {
                // no best distance !
                break;
            }
		
            // maxs = [5, 20, 100,...]
            // compute distances
            int d;
            Arrays.fill(distances, 0);
            for (int i = 0 ; i < nbMins ; i++) {
                for (int j = 1; j < differenceLevelsN; j++) {
                    if (i+j < nbMins) {
                        d = Math.abs(mins[i] - mins[i+j]);
                        distances[d] = distances[d] + 1;
                    }
                }
            }
            for (int i = 0 ; i < nbMaxs ; i++) {
                for (int j = 1; j < differenceLevelsN; j++) {
                    if (i+j < nbMaxs) {
                        d = Math.abs(maxs[i] - maxs[i+j]);
                        distances[d] = distances[d] + 1;
                    }
                }
            }
		
            // find best summed distance
            int bestDistance = -1;
            int bestValue = -1;
            for (int i = 0; i< curSamNb; i++) {
                int summed = 0;
                for (int j = (int) -delta ; j <= delta ; j++) {
                    if (i+j >=0 && i+j < curSamNb)
                        summed += distances[i+j];
                }
                if (summed == bestValue) {
                    if (i == 2*bestDistance)
                        bestDistance = i;
				
                } else if (summed > bestValue) {
                    bestValue = summed;
                    bestDistance = i;
                }
            }
		
            // averaging
            double distAvg = 0.0;
            double nbDists = 0;
            for (int j = (int) -delta ; j <= delta ; j++) {
                if (bestDistance+j >=0 && bestDistance+j < samplecount) {
                    int nbDist = distances[bestDistance+j];
                    if (nbDist > 0) {
                        nbDists += nbDist;
                        distAvg += (bestDistance+j)*nbDist;
                    }
                }
            }
            // this is our mode distance !
            distAvg /= nbDists;
		
            // continue the levels ?
            if (curModeDistance > -1.) {
                final double similarity = Math.abs(distAvg*2 - curModeDistance);
                if (similarity <= 2*delta) {
                    result = kSampleRateHz /(power2(curLevel-1)*curModeDistance);
                    break;
                }
            }

            // not similar, continue next level
            curModeDistance = distAvg;
		
            curLevel = curLevel + 1;
            if (curLevel >= maxFLWTlevels) {
                break;
            }
		
            // downsample
            if (curSamNb < 2) {
                break;
            }
            for (int i = 0; i < curSamNb/2; i++) {
                samples[i] = (samples[2*i] + samples[2*i + 1])/2.;
            }
            curSamNb /= 2;
	}

	return result;
    }

    // It states: 
    // - a pitch cannot change much all of a sudden (20%) (impossible humanly,
    //   so if such a situation happens, consider that it is a mistake and
    //   drop it. 
    //   (for music, it is quite common to have higher steps. allow for more)
    // - a pitch cannot double or be divided by 2 all of a sudden : it is an
    //   algorithm side-effect : divide it or double it by 2. 
    // - a lonely voiced pitch cannot happen, nor can a sudden drop in the middle
    //   of a voiced segment. Smooth the plot. 
    private double dynamicTracked(double pitch) {
        // equivalence
        if (pitch == 0.0) pitch = -1.0;

        //
        double estimatedPitch = -1;
        double acceptedError = 0.4f;   // used to be 0.2
        int maxConfidence = 5;

        if (pitch != -1) {
            // I have a pitch here

            if (prevPitch == -1) {
                // no previous
                estimatedPitch = pitch;
                prevPitch = pitch;
                pitchConfidence = 1;

            } else if (Math.abs(prevPitch - pitch) / pitch < acceptedError) {
                // similar : remember and increment pitch
                prevPitch = pitch;
                estimatedPitch = pitch;
                // maximum 3:
                pitchConfidence = Math.min(maxConfidence, pitchConfidence + 1);

            } else if ((pitchConfidence >= maxConfidence-2)
                    && (Math.abs(prevPitch - 2.*pitch)/(2.*pitch)
                    < acceptedError)) {
                // close to half the last pitch, which is trusted
                estimatedPitch = 2.*pitch;
                prevPitch = estimatedPitch;

            } else if ((pitchConfidence >= maxConfidence-2)
                    && Math.abs(prevPitch - 0.5*pitch)/(0.5*pitch) < acceptedError) {
                // close to twice the last pitch, which is trusted
                estimatedPitch = 0.5*pitch;
                prevPitch = estimatedPitch;
            } else {
                // nothing like this : very different value
                if (pitchConfidence >= 1) {
                    // previous trusted : keep previous
                    estimatedPitch = prevPitch;
                    pitchConfidence = Math.max(0, pitchConfidence - 1);
                } else {
                    // previous not trusted : take current
                    estimatedPitch = pitch;
                    prevPitch = pitch;
                    pitchConfidence = 1;
                }
            }

        } else {
            // no pitch now
            if (prevPitch != -1) {
                // was pitch before
                if (pitchConfidence >= 1) {
                    // continue previous
                    estimatedPitch = prevPitch;
                    pitchConfidence = Math.max(0, pitchConfidence - 1);
                } else {
                    prevPitch = -1;
                    estimatedPitch = -1.;
                    pitchConfidence = 0;
                }
            }
        }

        // put "_pitchConfidence="&pitchConfidence
        if (pitchConfidence >= 1) {
            // ok
            pitch = estimatedPitch;
        } else {
            pitch = -1;
        }

        // equivalence
        if (pitch == -1) pitch = 0.0;

        return pitch;
    }

    // TODO: we should not need this function, but directly shift in the loop.
    private int power2(int p) {
	int result = 1;
	for (int j = 0; j < p; j++)
            result <<= 1;
	return result;
    }

    private final int samplecount;
    private final int distances[];
    private final int mins[];
    private final int maxs[];

    private double prevPitch;
    private int pitchConfidence;
}
