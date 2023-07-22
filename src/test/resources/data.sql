select * from user t where
        id = :id and address = :address
-- #if :users != blank
    -- #for user  of :users  delimiter ' \nand ' filter ${user} != blank
        t.name = ${!user.name} ${cnd}
    -- #end
-- #fi
-- #if :userId <> :oldId && :info1 <> blank
    and id = 190
--#fi
-- #for user  of :users  delimiter ' \nand ' filter ${user} != blank
        t.name = ${user.age}
-- #end