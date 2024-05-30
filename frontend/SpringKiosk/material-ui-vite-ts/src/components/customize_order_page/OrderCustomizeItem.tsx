import { Avatar, Box, Button, Link, Typography } from "@mui/material";
import * as React from 'react';
import {
  unstable_useNumberInput as useNumberInput,
  UseNumberInputParameters,
} from '@mui/base/unstable_useNumberInput';
import { styled } from '@mui/system';
import { unstable_useForkRef as useForkRef } from '@mui/utils';
import ArrowDropUpRoundedIcon from '@mui/icons-material/ArrowDropUpRounded';
import ArrowDropDownRoundedIcon from '@mui/icons-material/ArrowDropDownRounded';
import { IMainDishIngredient } from "../../types/MenuTypes";
import { NewOrderContext } from "../../context/NewOrderContext";

interface OrderCustomizeItemProps {
    mainDishIngredient: IMainDishIngredient;
    ingredientIndex: number;
    menuIndex: number;
}

const OrderCustomizeItem = ({ mainDishIngredient, ingredientIndex, menuIndex }: OrderCustomizeItemProps) => {

  const CompactNumberInput = React.forwardRef(function CompactNumberInput(
    props: Omit<React.InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'value'> &
      UseNumberInputParameters,
    ref: React.ForwardedRef<HTMLInputElement>,
  ) {
    const {
      getRootProps,
      getInputProps,
      getIncrementButtonProps,
      getDecrementButtonProps,
    } = useNumberInput(props);
  
    const inputProps = getInputProps();
  
    inputProps.ref = useForkRef(inputProps.ref, ref);
  
    return (
      <StyledInputRoot {...getRootProps()}>
        <StyledStepperButton className="increment" {...getIncrementButtonProps()} id={`increment-${mainDishIngredient.ingredient.name}`}>
          <ArrowDropUpRoundedIcon />
        </StyledStepperButton>
        <StyledStepperButton className="decrement" {...getDecrementButtonProps()} id={`decrement-${mainDishIngredient.ingredient.name}`}>
          <ArrowDropDownRoundedIcon />
        </StyledStepperButton>
        <HiddenInput {...inputProps} />
      </StyledInputRoot>
    );
  });

  const blue = {
    100: '#DAECFF',
    200: '#80BFFF',
    400: '#3399FF',
    500: '#007FFF',
    600: '#0072E5',
    700: '#0059B2',
  };
  
  const grey = {
    50: '#F3F6F9',
    100: '#E5EAF2',
    200: '#DAE2ED',
    300: '#C7D0DD',
    400: '#B0B8C4',
    500: '#9DA8B7',
    600: '#6B7A90',
    700: '#434D5B',
    800: '#303740',
    900: '#1C2025',
  };
  
  const StyledInputRoot = styled('div')(
    ({ theme }) => `
      display: grid;
      grid-template-columns: 2rem;
      grid-template-rows: 2rem 2rem;
      grid-template-areas:
        "increment"
        "decrement";
      row-gap: 1px;
      overflow: auto;
      border-radius: 8px;
      border-style: solid;
      border-width: 1px;
      color: ${theme.palette.mode === 'dark' ? grey[300] : grey[900]};
      border-color: ${theme.palette.mode === 'dark' ? grey[800] : grey[200]};
      box-shadow: 0px 2px 4px ${
        theme.palette.mode === 'dark' ? 'rgba(0,0,0, 0.5)' : 'rgba(0,0,0, 0.05)'
      };
    `,
  );
  
  const HiddenInput = styled('input')`
    visibility: hidden;
    position: absolute;
  `;
  
  const StyledStepperButton = styled('button')(
    ({ theme }) => `
    display: flex;
    flex-flow: nowrap;
    justify-content: center;
    align-items: center;
    font-size: 0.875rem;
    box-sizing: border-box;
    border: 0;
    padding: 0;
    color: inherit;
    background: ${theme.palette.mode === 'dark' ? grey[900] : grey[50]};
  
    &:hover {
      cursor: pointer;
      background: ${theme.palette.mode === 'dark' ? blue[700] : blue[500]};
      color: ${grey[50]};
    }
  
    &:focus-visible {
      outline: 0;
      box-shadow: 0 0 0 3px ${theme.palette.mode === 'dark' ? blue[700] : blue[200]};
    }
  
    &.increment {
      grid-area: increment;
    }
  
    &.decrement {
      grid-area: decrement;
    }
  `,
  );
  
  const Layout = styled('div')`
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    column-gap: 1rem;
  `;
  
  const Pre = styled('pre')`
    font-size: 0.75rem;
  `;

  const {setOrder} = React.useContext(NewOrderContext);

  // Called when ingredients number is increased/decreased
  //    val -> new quantity
  const handleChange = (val: number | null) => {
    // update the value of this particular ingredient inside of the main dish of the given menu
    if (val != null && val >= 0 && val <= 9) {
      setOrder((prevOrder) => {
        const menuToUpdate = prevOrder.menus.find((menu, idx) => idx === menuIndex);
        if (!menuToUpdate) return prevOrder; // don't update if menu doesn't exist (shouldnt happen)
        const newOrderMenus = [...prevOrder.menus]
        menuToUpdate.selectedMainDish.mainDishIngredients[ingredientIndex].quantity = val;
        newOrderMenus[menuIndex] = menuToUpdate;
        return { menus: newOrderMenus };
      })
    }
  }

  return (
    <Box display={"flex"} sx={{ minHeight: 150 }} py={1} alignItems={"center"}>
      {/* <Box sx={{ width: "20%", height: "100%" }}>
        <Avatar style={{ maxWidth: "100%", maxHeight: "100%", minHeight: "100px", minWidth: "100px" }} src={ image } />
      </Box> */}
      <Box sx={{ width: "65%", height: "100%" }}>
        <Typography variant="h5">{ mainDishIngredient.ingredient.name }</Typography>
        <Typography>{ mainDishIngredient.ingredient.calories * mainDishIngredient.quantity } kcal</Typography>
        <Typography variant="caption">{mainDishIngredient.ingredient.calories} kcal/item</Typography>
      </Box>
      <Box component={Button} sx={{ width: "15%", height: "100%", fontStyle: "italic" }}>
        </Box>
      <Box
        sx={{ width: "20%", height: "100%" }}
        display={"flex"}
        flexDirection={"column"}
        textAlign={"center"}
        alignItems={"end"}
      >
        <Typography variant="h6">
            {(mainDishIngredient.ingredient.price * mainDishIngredient.quantity).toFixed(2)}€
        </Typography>
        <Typography variant="caption">({mainDishIngredient.ingredient.price.toFixed(2)}€/item)</Typography>

        <Layout style={{ 'marginTop': '15px'}}>
          <Pre>Quantity: {mainDishIngredient.quantity ?? ' '}</Pre>


          <CompactNumberInput
            aria-label="Compact number input"
            placeholder="Type a number…"
            readOnly
            value={mainDishIngredient.quantity}
            onChange={(event, val) => handleChange(val)}
          />
        </Layout>
      </Box>
    </Box>
  );
};

export default OrderCustomizeItem;
