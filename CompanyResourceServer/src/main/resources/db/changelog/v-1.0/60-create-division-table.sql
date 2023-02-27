create table division (
       division_id varchar(2) not null CONSTRAINT idchk CHECK (char_length(division_id)<=2),
        name varchar(950),
        primary key (division_id)
    )

GO

