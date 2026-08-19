package org.kamal.hotelservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HotelRequest {
    @NotBlank(message = "Hotel name is required!")
    private String name;
    @NotBlank(message = "Hotel address is required!")
    private String address;
    @NotBlank(message = "Hotel city is required!")
    private String city;
    @Min(value=1, message = "Hotel must have at least 1 star.")
    @Max(value=5, message = "Hotel cannot have more than 5 stars.")
    private int stars;
}
