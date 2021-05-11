package org.fastquery.bean.sunny;

import lombok.*;
import org.fastquery.core.Id;

@NoArgsConstructor
@Setter
@Getter
public class Card
{

    @Id
    private Integer id;
    private String number;


    public Card(String number)
    {
        this.number = number;
    }

    public Card(int id, String number)
    {
        this.id = id;
        this.number = number;
    }
}
