/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.rng.core.source32;

import org.apache.commons.rng.core.util.NumberFactory;
import org.apache.commons.rng.core.BaseProvider;

/**
 * Base class for all implementations that provide an {@code int}-based
 * source randomness.
 */
public abstract class IntProvider
    extends BaseProvider
    implements RandomIntSource {

    /**
     * Provides a bit source for booleans.
     *
     * <p>A cached value from a call to {@link #nextInt()}.
     */
    private int booleanSource; // Initialised as 0

    /**
     * The bit mask of the boolean source to obtain the boolean bit.
     *
     * <p>The bit mask contains a single bit set. This begins at the least
     * significant bit and is gradually shifted upwards until overflow to zero.
     *
     * <p>When zero a new boolean source should be created and the mask set to the
     * least significant bit (i.e. 1).
     */
    private int booleanBitMask; // Initialised as 0

    /**
     * Creates a new instance.
     */
    public IntProvider() {
        super();
    }

    /**
     * Creates a new instance copying the state from the source.
     *
     * <p>This provides base functionality to allow a generator to create a copy, for example
     * for use in the {@link org.apache.commons.rng.JumpableUniformRandomProvider
     * JumpableUniformRandomProvider} interface.
     *
     * @param source Source to copy.
     * @since 1.3
     */
    protected IntProvider(IntProvider source) {
        booleanSource = source.booleanSource;
        booleanBitMask = source.booleanBitMask;
    }

    /**
     * Reset the cached state used in the default implementation of {@link #nextBoolean()}.
     *
     * <p>This should be used when the state is no longer valid, for example after a jump
     * performed for the {@link org.apache.commons.rng.JumpableUniformRandomProvider
     * JumpableUniformRandomProvider} interface.</p>
     *
     * @since 1.3
     */
    protected void resetCachedState() {
        booleanSource = 0;
        booleanBitMask = 0;
    }

    /** {@inheritDoc} */
    @Override
    protected byte[] getStateInternal() {
        final int[] state = new int[] {booleanSource,
                                       booleanBitMask};
        return composeStateInternal(NumberFactory.makeByteArray(state),
                                    super.getStateInternal());
    }

    /** {@inheritDoc} */
    @Override
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, 8);
        final int[] state = NumberFactory.makeIntArray(c[0]);
        booleanSource  = state[0];
        booleanBitMask = state[1];
        super.setStateInternal(c[1]);
    }

    /** {@inheritDoc} */
    @Override
    public int nextInt() {
        return next();
    }

    /** {@inheritDoc} */
    @Override
    public boolean nextBoolean() {
        // Shift up. This will eventually overflow and become zero.
        booleanBitMask <<= 1;
        // The mask will either contain a single bit or none.
        if (booleanBitMask == 0) {
            // Set the least significant bit
            booleanBitMask = 1;
            // Get the next value
            booleanSource = nextInt();
        }
        // Return if the bit is set
        return (booleanSource & booleanBitMask) != 0;
    }

    /** {@inheritDoc} */
    @Override
    public double nextDouble() {
        return NumberFactory.makeDouble(nextInt(), nextInt());
    }

    /** {@inheritDoc} */
    @Override
    public float nextFloat() {
        return NumberFactory.makeFloat(nextInt());
    }

    /** {@inheritDoc} */
    @Override
    public long nextLong() {
        return NumberFactory.makeLong(nextInt(), nextInt());
    }

    /** {@inheritDoc} */
    @Override
    public void nextBytes(byte[] bytes) {
        nextBytesFill(this, bytes, 0, bytes.length);
    }

    /** {@inheritDoc} */
    @Override
    public void nextBytes(byte[] bytes,
                          int start,
                          int len) {
        checkIndex(0, bytes.length - 1, start);
        checkIndex(0, bytes.length - start, len);

        nextBytesFill(this, bytes, start, len);
    }

    /**
     * Generates random bytes and places them into a user-supplied array.
     *
     * <p>
     * The array is filled with bytes extracted from random {@code int} values.
     * This implies that the number of random bytes generated may be larger than
     * the length of the byte array.
     * </p>
     *
     * @param source Source of randomness.
     * @param bytes Array in which to put the generated bytes. Cannot be null.
     * @param start Index at which to start inserting the generated bytes.
     * @param len Number of bytes to insert.
     */
    static void nextBytesFill(RandomIntSource source,
                              byte[] bytes,
                              int start,
                              int len) {
        int index = start; // Index of first insertion.

        // Index of first insertion plus multiple of 4 part of length
        // (i.e. length with 2 least significant bits unset).
        final int indexLoopLimit = index + (len & 0x7ffffffc);

        // Start filling in the byte array, 4 bytes at a time.
        while (index < indexLoopLimit) {
            final int random = source.next();
            bytes[index++] = (byte) random;
            bytes[index++] = (byte) (random >>> 8);
            bytes[index++] = (byte) (random >>> 16);
            bytes[index++] = (byte) (random >>> 24);
        }

        final int indexLimit = start + len; // Index of last insertion + 1.

        // Fill in the remaining bytes.
        if (index < indexLimit) {
            int random = source.next();
            while (true) {
                bytes[index++] = (byte) random;
                if (index < indexLimit) {
                    random >>>= 8;
                } else {
                    break;
                }
            }
        }
    }
}