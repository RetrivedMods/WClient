package org.cloudburstmc.protocol.bedrock.data;

public enum GraphicsOverrideParameterType {

    SKY_ZENITH_COLOR,
    SKY_HORIZON_COLOR,
    HORIZON_BLEND_MIN,
    HORIZON_BLEND_MAX,
    HORIZON_BLEND_START,
    HORIZON_BLEND_MIE_START,
    RAYLEIGH_STRENGTH,
    SUN_MIE_STRENGTH,
    MOON_MIE_STRENGTH,
    SUN_GLARE_SHAPE,
    CHLOROPHYLL,
    CDOM,
    SUSPENDED_SEDIMENT,
    WAVES_DEPTH,
    WAVES_FREQUENCY,
    WAVES_FREQUENCY_SCALING,
    WAVES_SPEED,
    WAVES_SPEED_SCALING,
    WAVES_SHAPE,
    WAVES_OCTAVES,
    WAVES_MIX,
    WAVES_PULL,
    WAVES_DIRECTION_INCREMENT,
    MIDTONES_CONTRAST,
    HIGHLIGHTS_CONTRAST,
    SHADOWS_CONTRAST,
    /**
     * @since v944
     */
    HIGHLIGHTS_GAIN,
    /**
     * @since v944
     */
    HIGHLIGHTS_GAMMA,
    /**
     * @since v944
     */
    HIGHLIGHTS_OFFSET,
    /**
     * @since v944
     */
    HIGHLIGHTS_SATURATION,
    /**
     * @since v944
     */
    MIDTONES_GAIN,
    /**
     * @since v944
     */
    MIDTONES_GAMMA,
    /**
     * @since v944
     */
    MIDTONES_OFFSET,
    /**
     * @since v944
     */
    MIDTONES_SATURATION,
    /**
     * @since v944
     */
    SHADOWS_GAIN,
    /**
     * @since v944
     */
    SHADOWS_GAMMA,
    /**
     * @since v944
     */
    SHADOWS_OFFSET,
    /**
     * @since v944
     */
    SHADOWS_SATURATION,
    /**
     * @since v944
     */
    HIGHLIGHTS_MIN,
    /**
     * @since v944
     */
    SHADOWS_MAX,
    /**
     * @since v944
     */
    TEMPERATURE,
    /**
     * @since v944
     */
    SUN_COLOR,
    /**
     * @since v944
     */
    SUN_ILLUMINANCE,
    /**
     * @since v944
     */
    MOON_COLOR,
    /**
     * @since v944
     */
    MOON_ILLUMINANCE,
    /**
     * @since v944
     */
    FLASH_COLOR,
    /**
     * @since v944
     */
    FLASH_ILLUMINANCE,
    /**
     * @since v944
     */
    AMBIENT_COLOR,
    /**
     * @since v944
     */
    AMBIENT_ILLUMINANCE,
    /**
     * @since v975
     */
    EMISSIVE_DESATURATION,
    /**
     * @since v975
     */
    SKY_INTENSITY,
    /**
     * @since v975
     */
    ORBITAL_OFFSET_DEGREES,
}
